package cn.bg7qvu.mtrwebctc.metrics;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * 指标收集器
 * 收集 API 请求、错误、延迟等指标
 */
public class MetricsCollector {
    // 请求计数器
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    
    // 端点统计
    private final Map<String, EndpointStats> endpointStats = new ConcurrentHashMap<>();
    
    // 响应时间分布
    private final Map<String, AtomicLong> responseTimeBuckets = new ConcurrentHashMap<>();
    
    // 错误计数
    private final Map<Integer, AtomicLong> errorCodes = new ConcurrentHashMap<>();
    
    // WebSocket 连接数
    private final AtomicInteger wsConnections = new AtomicInteger(0);
    
    // 启动时间
    private final long startTime = System.currentTimeMillis();
    
    // 响应时间桶边界（毫秒）
    private static final long[] TIME_BUCKETS = {10, 50, 100, 250, 500, 1000, 2500, 5000, 10000};
    private static final String[] BUCKET_NAMES = {"10ms", "50ms", "100ms", "250ms", "500ms", "1s", "2.5s", "5s", "10s", ">10s"};
    
    /**
     * 记录请求
     */
    public void recordRequest(String endpoint, String method, int statusCode, long durationMs) {
        totalRequests.incrementAndGet();
        
        if (statusCode >= 200 && statusCode < 400) {
            successRequests.incrementAndGet();
        } else {
            failedRequests.incrementAndGet();
            errorCodes.computeIfAbsent(statusCode, k -> new AtomicLong(0)).incrementAndGet();
        }
        
        // 端点统计
        String key = method + " " + endpoint;
        endpointStats.computeIfAbsent(key, k -> new EndpointStats()).record(statusCode, durationMs);
        
        // 响应时间分布
        String bucket = getBucket(durationMs);
        responseTimeBuckets.computeIfAbsent(bucket, k -> new AtomicLong(0)).incrementAndGet();
    }
    
    /**
     * WebSocket 连接增加
     */
    public void wsConnect() {
        wsConnections.incrementAndGet();
    }
    
    /**
     * WebSocket 连接减少
     */
    public void wsDisconnect() {
        wsConnections.decrementAndGet();
    }
    
    /**
     * 获取指标摘要
     */
    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        
        // 基本信息
        summary.put("uptime_ms", System.currentTimeMillis() - startTime);
        summary.put("uptime_human", formatUptime(System.currentTimeMillis() - startTime));
        
        // 请求统计
        Map<String, Object> requests = new LinkedHashMap<>();
        requests.put("total", totalRequests.get());
        requests.put("success", successRequests.get());
        requests.put("failed", failedRequests.get());
        requests.put("success_rate", calculateRate(successRequests.get(), totalRequests.get()));
        summary.put("requests", requests);
        
        // WebSocket
        summary.put("websocket_connections", wsConnections.get());
        
        // 响应时间分布
        Map<String, Long> timeDistribution = new LinkedHashMap<>();
        for (String name : BUCKET_NAMES) {
            timeDistribution.put(name, responseTimeBuckets.getOrDefault(name, new AtomicLong(0)).get());
        }
        summary.put("response_time_distribution", timeDistribution);
        
        // 错误码统计
        Map<String, Long> errors = new LinkedHashMap<>();
        errorCodes.forEach((code, count) -> errors.put(code.toString(), count.get()));
        summary.put("error_codes", errors);
        
        return summary;
    }
    
    /**
     * 获取端点统计
     */
    public Map<String, Object> getEndpointStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        
        endpointStats.forEach((endpoint, stat) -> {
            Map<String, Object> endpointData = new LinkedHashMap<>();
            endpointData.put("total_requests", stat.totalRequests.get());
            endpointData.put("avg_latency_ms", stat.calculateAvgLatency());
            endpointData.put("max_latency_ms", stat.maxLatency.get());
            endpointData.put("error_count", stat.errorCount.get());
            stats.put(endpoint, endpointData);
        });
        
        return stats;
    }
    
    /**
     * 重置指标
     */
    public void reset() {
        totalRequests.set(0);
        successRequests.set(0);
        failedRequests.set(0);
        endpointStats.clear();
        responseTimeBuckets.clear();
        errorCodes.clear();
    }
    
    // 辅助方法
    private String getBucket(long durationMs) {
        for (int i = 0; i < TIME_BUCKETS.length; i++) {
            if (durationMs < TIME_BUCKETS[i]) {
                return BUCKET_NAMES[i];
            }
        }
        return BUCKET_NAMES[BUCKET_NAMES.length - 1];
    }
    
    private double calculateRate(long success, long total) {
        return total > 0 ? (double) success / total * 100 : 0;
    }
    
    private String formatUptime(long uptimeMs) {
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) return String.format("%dd %dh", days, hours % 24);
        if (hours > 0) return String.format("%dh %dm", hours, minutes % 60);
        if (minutes > 0) return String.format("%dm %ds", minutes, seconds % 60);
        return String.format("%ds", seconds);
    }
    
    // 端点统计类
    private static class EndpointStats {
        final AtomicLong totalRequests = new AtomicLong(0);
        final AtomicLong errorCount = new AtomicLong(0);
        final AtomicLong totalLatency = new AtomicLong(0);
        final AtomicLong maxLatency = new AtomicLong(0);
        final AtomicLong minLatency = new AtomicLong(Long.MAX_VALUE);
        
        void record(int statusCode, long latencyMs) {
            totalRequests.incrementAndGet();
            totalLatency.addAndGet(latencyMs);
            
            // 更新最大延迟
            long currentMax;
            do {
                currentMax = maxLatency.get();
                if (latencyMs <= currentMax) break;
            } while (!maxLatency.compareAndSet(currentMax, latencyMs));
            
            // 更新最小延迟
            long currentMin;
            do {
                currentMin = minLatency.get();
                if (latencyMs >= currentMin) break;
            } while (!minLatency.compareAndSet(currentMin, latencyMs));
            
            // 错误计数
            if (statusCode >= 400) {
                errorCount.incrementAndGet();
            }
        }
        
        double calculateAvgLatency() {
            long total = totalRequests.get();
            return total > 0 ? (double) totalLatency.get() / total : 0;
        }
    }
}
