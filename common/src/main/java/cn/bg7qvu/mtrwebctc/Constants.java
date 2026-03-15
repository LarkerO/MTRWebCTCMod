package cn.bg7qvu.mtrwebctc;

/**
 * 模组常量定义
 */
public final class Constants {
    private Constants() {}
    
    // 模组信息
    public static final String MOD_ID = "mtrwebctc";
    public static final String MOD_NAME = "MTRWebCTC";
    public static final String VERSION = "1.0.0";
    
    // API 版本
    public static final String API_VERSION = "v1";
    public static final String API_PREFIX = "/api/" + API_VERSION;
    
    // 默认配置
    public static final int DEFAULT_PORT = 7044;
    public static final String DEFAULT_BIND = "0.0.0.0";
    public static final long DEFAULT_TOKEN_VALIDITY_MS = 24 * 60 * 60 * 1000L; // 24 小时
    
    // 列车追踪
    public static final long DEFAULT_POSITION_UPDATE_INTERVAL_MS = 1000L;
    public static final long DEFAULT_HISTORY_RETENTION_MINUTES = 5L;
    public static final long DEFAULT_WS_PUSH_INTERVAL_MS = 15000L;
    
    // 备份
    public static final boolean DEFAULT_BACKUP_ENABLED = true;
    public static final int DEFAULT_MAX_BACKUPS = 3;
    
    // 速率限制
    public static final int DEFAULT_RATE_LIMIT_REQUESTS = 100;
    public static final long DEFAULT_RATE_LIMIT_WINDOW_MS = 60000L;
    
    // 支持的 MTR 版本
    public static final String MTR_MIN_VERSION = "3.0.0";
    public static final String MTR_MAX_VERSION = "3.99.99";
    
    // 支持的 Minecraft 版本
    public static final String[] SUPPORTED_MC_VERSIONS = {
        "1.16.5",
        "1.18.2",
        "1.20.1"
    };
    
    // 日志标签
    public static final String LOG_TAG = "[" + MOD_NAME + "]";
    
    // 配置文件路径
    public static final String CONFIG_DIR = "config/" + MOD_ID;
    public static final String CONFIG_FILE = CONFIG_DIR + "/config.json";
    public static final String BACKUP_DIR = CONFIG_DIR + "/backups";
    public static final String LOG_FILE = CONFIG_DIR + "/debug.log";
    
    // HTTP 头
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_X_RATELIMIT_LIMIT = "X-RateLimit-Limit";
    public static final String HEADER_X_RATELIMIT_REMAINING = "X-RateLimit-Remaining";
    public static final String HEADER_X_RATELIMIT_RESET = "X-RateLimit-Reset";
    
    // WebSocket 消息类型
    public static final String WS_MSG_SUBSCRIBE = "subscribe";
    public static final String WS_MSG_UNSUBSCRIBE = "unsubscribe";
    public static final String WS_MSG_PING = "ping";
    public static final String WS_MSG_PONG = "pong";
    
    // 错误消息
    public static final String ERROR_UNAUTHORIZED = "Unauthorized";
    public static final String ERROR_INVALID_TOKEN = "Invalid or expired token";
    public static final String ERROR_NOT_FOUND = "Resource not found";
    public static final String ERROR_BAD_REQUEST = "Bad request";
    public static final String ERROR_RATE_LIMITED = "Too many requests";
    public static final String ERROR_INTERNAL = "Internal server error";
}
