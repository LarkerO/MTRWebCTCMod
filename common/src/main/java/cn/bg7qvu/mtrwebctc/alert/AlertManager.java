package cn.bg7qvu.mtrwebctc.alert;

import cn.bg7qvu.mtrwebctc.model.AlertDTO;
import cn.bg7qvu.mtrwebctc.util.Logger;
import cn.bg7qvu.mtrwebctc.websocket.WebSocketHandler;

import java.util.*;
import java.util.concurrent.*;

/**
 * 告警管理器
 */
public class AlertManager {
    private final List<AlertDTO> alerts = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, AlertDTO> activeAlerts = new ConcurrentHashMap<>();
    private final WebSocketHandler wsHandler;
    private final int maxAlerts;
    
    public AlertManager(WebSocketHandler wsHandler, int maxAlerts) {
        this.wsHandler = wsHandler;
        this.maxAlerts = maxAlerts;
    }
    
    public AlertManager(WebSocketHandler wsHandler) {
        this(wsHandler, 1000);
    }
    
    /**
     * 创建告警
     */
    public AlertDTO createAlert(String type, String severity, String title, String message) {
        AlertDTO alert = AlertDTO.builder()
            .type(type)
            .severity(severity)
            .title(title)
            .message(message)
            .build();
        
        alerts.add(0, alert);
        activeAlerts.put(alert.getId(), alert);
        
        // 限制数量
        while (alerts.size() > maxAlerts) {
            alerts.remove(alerts.size() - 1);
        }
        
        Logger.warn("Alert created: [" + severity + "] " + title);
        
        // 推送 WebSocket
        if (wsHandler != null) {
            wsHandler.broadcast("alerts", Map.of("event", "created", "alert", alert));
        }
        
        return alert;
    }
    
    /**
     * 便捷方法
     */
    public AlertDTO info(String title, String message) {
        return createAlert(AlertDTO.Type.SYSTEM_ERROR, AlertDTO.Severity.INFO, title, message);
    }
    
    public AlertDTO warning(String title, String message) {
        return createAlert(AlertDTO.Type.SYSTEM_ERROR, AlertDTO.Severity.WARNING, title, message);
    }
    
    public AlertDTO error(String title, String message) {
        return createAlert(AlertDTO.Type.SYSTEM_ERROR, AlertDTO.Severity.ERROR, title, message);
    }
    
    public AlertDTO critical(String title, String message) {
        return createAlert(AlertDTO.Type.SYSTEM_ERROR, AlertDTO.Severity.CRITICAL, title, message);
    }
    
    /**
     * 确认告警
     */
    public boolean acknowledgeAlert(String alertId, String acknowledgedBy) {
        AlertDTO alert = activeAlerts.get(alertId);
        if (alert == null) {
            return false;
        }
        
        alert.acknowledge(acknowledgedBy);
        
        Logger.info("Alert acknowledged: " + alertId + " by " + acknowledgedBy);
        
        // 推送 WebSocket
        if (wsHandler != null) {
            wsHandler.broadcast("alerts", Map.of("event", "acknowledged", "alert", alert));
        }
        
        return true;
    }
    
    /**
     * 获取告警
     */
    public AlertDTO getAlert(String alertId) {
        return activeAlerts.get(alertId);
    }
    
    /**
     * 获取所有活跃告警
     */
    public List<AlertDTO> getActiveAlerts() {
        return new ArrayList<>(activeAlerts.values());
    }
    
    /**
     * 获取未确认告警
     */
    public List<AlertDTO> getUnacknowledgedAlerts() {
        return activeAlerts.values().stream()
            .filter(a -> !a.isAcknowledged())
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 获取所有告警（分页）
     */
    public List<AlertDTO> getAlerts(int limit, int offset) {
        return alerts.stream()
            .skip(offset)
            .limit(limit)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 按严重程度获取
     */
    public List<AlertDTO> getAlertsBySeverity(String severity) {
        return alerts.stream()
            .filter(a -> severity.equals(a.getSeverity()))
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 按类型获取
     */
    public List<AlertDTO> getAlertsByType(String type) {
        return alerts.stream()
            .filter(a -> type.equals(a.getType()))
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 清除已确认的告警
     */
    public int clearAcknowledged() {
        int removed = 0;
        Iterator<AlertDTO> it = activeAlerts.values().iterator();
        while (it.hasNext()) {
            if (it.next().isAcknowledged()) {
                it.remove();
                removed++;
            }
        }
        return removed;
    }
    
    /**
     * 清除所有告警
     */
    public void clearAll() {
        alerts.clear();
        activeAlerts.clear();
    }
    
    /**
     * 获取统计
     */
    public Map<String, Object> getStats() {
        Map<String, Long> bySeverity = new LinkedHashMap<>();
        bySeverity.put(AlertDTO.Severity.INFO, 0L);
        bySeverity.put(AlertDTO.Severity.WARNING, 0L);
        bySeverity.put(AlertDTO.Severity.ERROR, 0L);
        bySeverity.put(AlertDTO.Severity.CRITICAL, 0L);
        
        for (AlertDTO alert : activeAlerts.values()) {
            bySeverity.merge(alert.getSeverity(), 1L, Long::sum);
        }
        
        return Map.of(
            "total", alerts.size(),
            "active", activeAlerts.size(),
            "unacknowledged", getUnacknowledgedAlerts().size(),
            "by_severity", bySeverity
        );
    }
}
