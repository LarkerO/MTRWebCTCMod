package cn.bg7qvu.mtrwebctc.security;

import cn.bg7qvu.mtrwebctc.util.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 速率限制器
 * 用于防止 API 滥用
 */
public class RateLimiter {
    private final int maxRequests;
    private final long windowMs;
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> windowStarts = new ConcurrentHashMap<>();
    
    public RateLimiter(int maxRequests, long windowMs) {
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
    }
    
    /**
     * 检查是否允许请求
     * @param clientId 客户端标识（IP 或 token）
     * @return 是否允许
     */
    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        
        // 获取或创建窗口开始时间
        Long windowStart = windowStarts.computeIfAbsent(clientId, k -> now);
        
        // 检查是否需要重置窗口
        if (now - windowStart > windowMs) {
            windowStarts.put(clientId, now);
            requestCounts.put(clientId, new AtomicInteger(0));
            windowStart = now;
        }
        
        // 增加请求计数
        AtomicInteger count = requestCounts.computeIfAbsent(clientId, k -> new AtomicInteger(0));
        int currentCount = count.incrementAndGet();
        
        // 检查是否超过限制
        if (currentCount > maxRequests) {
            Logger.warn("Rate limit exceeded for " + clientId + ": " + currentCount + " requests");
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取剩余请求数
     * @param clientId 客户端标识
     * @return 剩余请求数
     */
    public int getRemainingRequests(String clientId) {
        AtomicInteger count = requestCounts.get(clientId);
        if (count == null) {
            return maxRequests;
        }
        
        Long windowStart = windowStarts.get(clientId);
        if (windowStart == null || System.currentTimeMillis() - windowStart > windowMs) {
            return maxRequests;
        }
        
        return Math.max(0, maxRequests - count.get());
    }
    
    /**
     * 获取重置时间（毫秒）
     * @param clientId 客户端标识
     * @return 重置时间
     */
    public long getResetTime(String clientId) {
        Long windowStart = windowStarts.get(clientId);
        if (windowStart == null) {
            return 0;
        }
        
        return Math.max(0, windowMs - (System.currentTimeMillis() - windowStart));
    }
    
    /**
     * 清理过期的窗口
     */
    public void cleanup() {
        long now = System.currentTimeMillis();
        
        requestCounts.keySet().removeIf(clientId -> {
            Long windowStart = windowStarts.get(clientId);
            return windowStart == null || now - windowStart > windowMs * 2;
        });
        
        windowStarts.keySet().removeIf(clientId -> !requestCounts.containsKey(clientId));
    }
    
    /**
     * 重置所有限制
     */
    public void reset() {
        requestCounts.clear();
        windowStarts.clear();
        Logger.info("Rate limiter reset");
    }
    
    /**
     * 获取活跃客户端数量
     * @return 客户端数量
     */
    public int getActiveClientCount() {
        return requestCounts.size();
    }
}
