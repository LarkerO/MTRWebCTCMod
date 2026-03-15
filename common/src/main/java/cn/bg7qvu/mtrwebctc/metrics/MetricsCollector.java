package cn.bg7qvu.mtrwebctc.metrics;

import cn.bg7qvu.mtrwebctc.util.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 指标收集器
 * 用于收集和暴露应用指标
 */
public class MetricsCollector {
    private final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();
    private final Map<String, Long> gauges = new ConcurrentHashMap<>();
    private final Map<String, Long> timers = new ConcurrentHashMap<>();
    
    private long startTime = System.currentTimeMillis();
    
    /**
     * 增加计数器
     * @param name 计数器名称
     */
    public void increment(String name) {
        increment(name, 1);
    }
    
    /**
     * 增加计数器
     * @param name 计数器名称
     * @param value 增量
     */
    public void increment(String name, long value) {
        counters.computeIfAbsent(name, k -> new AtomicLong(0)).addAndGet(value);
    }
    
    /**
     * 设置仪表值
     * @param name 仪表名称
     * @param value 值
     */
    public void setGauge(String name, long value) {
        gauges.put(name, value);
    }
    
    /**
     * 记录计时
     * @param name 计时器名称
     * @param durationMs 持续时间（毫秒）
     */
    public void recordTime(String name, long durationMs) {
        timers.put(name, durationMs);
    }
    
    /**
     * 开始计时
     * @return 开始时间戳
     */
    public long startTimer() {
        return System.currentTimeMillis();
    }
    
    /**
     * 结束计时并记录
     * @param name 计时器名称
     * @param startTime 开始时间戳
     */
    public void stopTimer(String name, long startTime) {
        recordTime(name, System.currentTimeMillis() - startTime);
    }
    
    /**
     * 获取计数器值
     * @param name 计数器名称
     * @return 值
     */
    public long getCounter(String name) {
        AtomicLong counter = counters.get(name);
        return counter != null ? counter.get() : 0;
    }
    
    /**
     * 获取仪表值
     * @param name 仪表名称
     * @return 值
     */
    public long getGauge(String name) {
        return gauges.getOrDefault(name, 0L);
    }
    
    /**
     * 获取计时值
     * @param name 计时器名称
     * @return 值（毫秒）
     */
    public long getTimer(String name) {
        return timers.getOrDefault(name, 0L);
    }
    
    /**
     * 获取运行时间（秒）
     * @return 运行时间
     */
    public long getUptimeSeconds() {
        return (System.currentTimeMillis() - startTime) / 1000;
    }
    
    /**
     * 导出所有指标
     * @return 指标映射
     */
    public Map<String, Object> export() {
        Map<String, Object> result = new ConcurrentHashMap<>();
        
        result.put("uptime_seconds", getUptimeSeconds());
        result.put("start_time", startTime);
        result.put("timestamp", System.currentTimeMillis());
        
        // 计数器
        Map<String, Long> counterMap = new ConcurrentHashMap<>();
        counters.forEach((k, v) -> counterMap.put(k, v.get()));
        result.put("counters", counterMap);
        
        // 仪表
        result.put("gauges", new ConcurrentHashMap<>(gauges));
        
        // 计时器
        result.put("timers", new ConcurrentHashMap<>(timers));
        
        return result;
    }
    
    /**
     * 重置所有指标
     */
    public void reset() {
        counters.clear();
        gauges.clear();
        timers.clear();
        startTime = System.currentTimeMillis();
        Logger.info("Metrics reset");
    }
    
    /**
     * 记录 API 请求
     * @param endpoint 端点
     * @param method HTTP 方法
     * @param statusCode 状态码
     * @param durationMs 持续时间
     */
    public void recordApiRequest(String endpoint, String method, int statusCode, long durationMs) {
        increment("api_requests_total");
        increment("api_requests_" + method.toLowerCase());
        increment("api_requests_status_" + statusCode);
        recordTime("api_request_" + endpoint.replace("/", "_"), durationMs);
    }
    
    /**
     * 记录 WebSocket 连接
     */
    public void recordWebSocketConnection() {
        increment("websocket_connections_total");
    }
    
    /**
     * 记录 WebSocket 断开
     */
    public void recordWebSocketDisconnection() {
        increment("websocket_disconnections_total");
    }
}
