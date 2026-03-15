package cn.bg7qvu.mtrwebctc.util;

import java.util.*;
import java.util.concurrent.*;

/**
 * 缓存工具类
 */
public class Cache<K, V> {
    private final ConcurrentHashMap<K, CacheEntry<V>> cache = new ConcurrentHashMap<>();
    private final long defaultTtlMs;
    private final int maxSize;
    
    private final ScheduledExecutorService cleanupExecutor;
    
    /**
     * 创建缓存
     * @param defaultTtlMs 默认过期时间（毫秒）
     * @param maxSize 最大容量
     */
    public Cache(long defaultTtlMs, int maxSize) {
        this.defaultTtlMs = defaultTtlMs;
        this.maxSize = maxSize;
        
        // 定期清理过期条目
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Cache-Cleanup");
            t.setDaemon(true);
            return t;
        });
        
        this.cleanupExecutor.scheduleAtFixedRate(
            this::cleanup,
            defaultTtlMs / 2,
            defaultTtlMs / 2,
            TimeUnit.MILLISECONDS
        );
    }
    
    /**
     * 创建无过期缓存
     */
    public Cache(int maxSize) {
        this(Long.MAX_VALUE, maxSize);
    }
    
    /**
     * 获取
     */
    public V get(K key) {
        CacheEntry<V> entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        
        if (entry.isExpired()) {
            cache.remove(key);
            return null;
        }
        
        return entry.value;
    }
    
    /**
     * 获取，如果不存在则计算
     */
    public V getOrCompute(K key, java.util.function.Supplier<V> supplier) {
        return getOrCompute(key, defaultTtlMs, supplier);
    }
    
    public V getOrCompute(K key, long ttlMs, java.util.function.Supplier<V> supplier) {
        V value = get(key);
        if (value != null) {
            return value;
        }
        
        value = supplier.get();
        if (value != null) {
            put(key, value, ttlMs);
        }
        
        return value;
    }
    
    /**
     * 存入
     */
    public void put(K key, V value) {
        put(key, value, defaultTtlMs);
    }
    
    public void put(K key, V value, long ttlMs) {
        if (cache.size() >= maxSize) {
            evictOldest();
        }
        
        cache.put(key, new CacheEntry<>(value, System.currentTimeMillis() + ttlMs));
    }
    
    /**
     * 移除
     */
    public V remove(K key) {
        CacheEntry<V> entry = cache.remove(key);
        return entry != null ? entry.value : null;
    }
    
    /**
     * 清空
     */
    public void clear() {
        cache.clear();
    }
    
    /**
     * 大小
     */
    public int size() {
        return cache.size();
    }
    
    /**
     * 是否包含
     */
    public boolean contains(K key) {
        return get(key) != null;
    }
    
    /**
     * 获取所有键
     */
    public Set<K> keys() {
        return new HashSet<>(cache.keySet());
    }
    
    /**
     * 清理过期条目
     */
    private void cleanup() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * 淘汰最旧条目
     */
    private void evictOldest() {
        K oldestKey = null;
        long oldestTime = Long.MAX_VALUE;
        
        for (Map.Entry<K, CacheEntry<V>> entry : cache.entrySet()) {
            if (entry.getValue().expireTime < oldestTime) {
                oldestTime = entry.getValue().expireTime;
                oldestKey = entry.getKey();
            }
        }
        
        if (oldestKey != null) {
            cache.remove(oldestKey);
        }
    }
    
    /**
     * 关闭
     */
    public void shutdown() {
        cleanupExecutor.shutdown();
        cache.clear();
    }
    
    /**
     * 缓存条目
     */
    private static class CacheEntry<V> {
        final V value;
        final long expireTime;
        
        CacheEntry(V value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }
}
