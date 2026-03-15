package cn.bg7qvu.mtrwebctc.security;

import cn.bg7qvu.mtrwebctc.util.Logger;

import java.util.*;
import java.util.concurrent.*;

/**
 * 请求速率限制器
 * 基于令牌桶算法
 */
public class RateLimiter {
    // IP -> 令牌桶
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    
    // 配置
    private final int maxRequests;
    private final long windowMs;
    private final int burstSize;
    
    // 清理任务
    private final ScheduledExecutorService cleaner;
    
    /**
     * 创建速率限制器
     * @param maxRequests 时间窗口内最大请求数
     * @param windowMs 时间窗口（毫秒）
     * @param burstSize 突发请求大小
     */
    public RateLimiter(int maxRequests, long windowMs, int burstSize) {
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
        this.burstSize = burstSize;
        
        // 定期清理过期桶
        this.cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "RateLimiter-Cleaner");
            t.setDaemon(true);
            return t;
        });
        
        this.cleaner.scheduleAtFixedRate(this::cleanup, 60000, 60000, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 检查是否允许请求
     * @param clientIp 客户端 IP
     * @return true 如果允许，false 如果被限流
     */
    public boolean allowRequest(String clientIp) {
        TokenBucket bucket = buckets.computeIfAbsent(clientIp, k -> new TokenBucket(maxRequests, windowMs, burstSize));
        return bucket.tryAcquire();
    }
    
    /**
     * 获取客户端剩余配额
     */
    public int getRemainingQuota(String clientIp) {
        TokenBucket bucket = buckets.get(clientIp);
        return bucket != null ? bucket.getRemaining() : maxRequests;
    }
    
    /**
     * 获取客户端重置时间
     */
    public long getResetTime(String clientIp) {
        TokenBucket bucket = buckets.get(clientIp);
        return bucket != null ? bucket.getResetTime() : System.currentTimeMillis() + windowMs;
    }
    
    /**
     * 清理过期桶
     */
    private void cleanup() {
        long now = System.currentTimeMillis();
        long threshold = now - windowMs * 2;
        
        buckets.entrySet().removeIf(entry -> entry.getValue().getLastAccessTime() < threshold);
        
        Logger.debug("Rate limiter cleanup: " + buckets.size() + " active buckets");
    }
    
    /**
     * 关闭
     */
    public void shutdown() {
        cleaner.shutdown();
    }
    
    /**
     * 获取统计信息
     */
    public Map<String, Object> getStats() {
        return Map.of(
            "active_clients", buckets.size(),
            "max_requests", maxRequests,
            "window_ms", windowMs,
            "burst_size", burstSize
        );
    }
    
    /**
     * 令牌桶实现
     */
    private static class TokenBucket {
        private final int capacity;
        private final long refillIntervalMs;
        private final int refillAmount;
        
        private volatile int tokens;
        private volatile long lastRefillTime;
        private volatile long lastAccessTime;
        
        TokenBucket(int capacity, long windowMs, int burstSize) {
            this.capacity = capacity;
            this.refillIntervalMs = windowMs;
            this.refillAmount = capacity;
            this.tokens = burstSize > 0 ? Math.min(burstSize, capacity) : capacity;
            this.lastRefillTime = System.currentTimeMillis();
            this.lastAccessTime = lastRefillTime;
        }
        
        synchronized boolean tryAcquire() {
            lastAccessTime = System.currentTimeMillis();
            refill();
            
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }
        
        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTime;
            
            if (elapsed >= refillIntervalMs) {
                tokens = Math.min(capacity, tokens + refillAmount);
                lastRefillTime = now;
            }
        }
        
        int getRemaining() {
            refill();
            return tokens;
        }
        
        long getResetTime() {
            return lastRefillTime + refillIntervalMs;
        }
        
        long getLastAccessTime() {
            return lastAccessTime;
        }
    }
}
