package cn.bg7qvu.mtrwebctc.model;

import java.util.*;

/**
 * 告警 DTO
 */
public class AlertDTO {
    private String id;
    private String type;
    private String severity;
    private String title;
    private String message;
    private long timestamp;
    private boolean acknowledged;
    private String acknowledgedBy;
    private long acknowledgedAt;
    private Map<String, Object> metadata;
    
    /**
     * 告警类型
     */
    public static class Type {
        public static final String TRAIN_STALLED = "train_stalled";
        public static final String TRAIN_OFF_ROUTE = "train_off_route";
        public static final String DEPOT_EMPTY = "depot_empty";
        public static final String SCHEDULE_DELAY = "schedule_delay";
        public static final String SYSTEM_ERROR = "system_error";
        public static final String BACKUP_FAILED = "backup_failed";
    }
    
    /**
     * 严重程度
     */
    public static class Severity {
        public static final String INFO = "info";
        public static final String WARNING = "warning";
        public static final String ERROR = "error";
        public static final String CRITICAL = "critical";
    }
    
    // Getters
    public String getId() { return id; }
    public String getType() { return type; }
    public String getSeverity() { return severity; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
    public boolean isAcknowledged() { return acknowledged; }
    public String getAcknowledgedBy() { return acknowledgedBy; }
    public long getAcknowledgedAt() { return acknowledgedAt; }
    public Map<String, Object> getMetadata() { return metadata; }
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setType(String type) { this.type = type; }
    public void setSeverity(String severity) { this.severity = severity; }
    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }
    public void setAcknowledgedBy(String acknowledgedBy) { this.acknowledgedBy = acknowledgedBy; }
    public void setAcknowledgedAt(long acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    /**
     * 确认告警
     */
    public void acknowledge(String by) {
        this.acknowledged = true;
        this.acknowledgedBy = by;
        this.acknowledgedAt = System.currentTimeMillis();
    }
    
    /**
     * Builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private AlertDTO alert = new AlertDTO();
        
        public Builder id(String id) { alert.id = id; return this; }
        public Builder type(String type) { alert.type = type; return this; }
        public Builder severity(String severity) { alert.severity = severity; return this; }
        public Builder title(String title) { alert.title = title; return this; }
        public Builder message(String message) { alert.message = message; return this; }
        public Builder timestamp(long timestamp) { alert.timestamp = timestamp; return this; }
        public Builder metadata(Map<String, Object> metadata) { alert.metadata = metadata; return this; }
        
        public AlertDTO build() {
            if (alert.id == null) {
                alert.id = UUID.randomUUID().toString();
            }
            if (alert.timestamp == 0) {
                alert.timestamp = System.currentTimeMillis();
            }
            return alert;
        }
    }
}
