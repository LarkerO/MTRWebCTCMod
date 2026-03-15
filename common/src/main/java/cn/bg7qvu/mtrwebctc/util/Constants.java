package cn.bg7qvu.mtrwebctc.util;

/**
 * 常量定义
 */
public final class Constants {
    private Constants() {}
    
    // ==================== 模组信息 ====================
    public static final String MOD_ID = "mtrwebctc";
    public static final String MOD_NAME = "MTRWebCTC";
    public static final String MOD_VERSION = "1.0.0";
    
    // ==================== 默认配置 ====================
    public static final int DEFAULT_PORT = 7044;
    public static final String DEFAULT_BIND = "0.0.0.0";
    public static final int MAX_BACKUPS = 3;
    public static final int TOKEN_VALIDITY_HOURS = 24;
    public static final int POSITION_UPDATE_INTERVAL_MS = 1000;
    public static final int WEBSOCKET_PUSH_INTERVAL_MS = 15000;
    public static final int HISTORY_RETENTION_MINUTES = 5;
    
    // ==================== API 路径 ====================
    public static final String API_BASE_PATH = "/api";
    public static final String HEALTH_PATH = "/health";
    public static final String WEBSOCKET_PATH = "/ws";
    
    // ==================== HTTP 头 ====================
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String BEARER_PREFIX = "Bearer ";
    
    // ==================== 内容类型 ====================
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_HTML = "text/html";
    public static final String CONTENT_TYPE_CSS = "text/css";
    public static final String CONTENT_TYPE_JS = "application/javascript";
    
    // ==================== 错误消息 ====================
    public static final String ERROR_UNAUTHORIZED = "Unauthorized";
    public static final String ERROR_NOT_FOUND = "Not found";
    public static final String ERROR_INVALID_REQUEST = "Invalid request";
    public static final String ERROR_SERVER_ERROR = "Internal server error";
    
    // ==================== 日志标签 ====================
    public static final String LOG_TAG = "[" + MOD_NAME + "]";
}
