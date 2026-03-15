package cn.bg7qvu.mtrwebctc.exception;

/**
 * 模组异常基类
 */
public class MTRWebCTCException extends RuntimeException {
    private final String errorCode;
    
    public MTRWebCTCException(String message) {
        super(message);
        this.errorCode = "UNKNOWN";
    }
    
    public MTRWebCTCException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public MTRWebCTCException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * 配置错误
     */
    public static class ConfigException extends MTRWebCTCException {
        public ConfigException(String message) {
            super("CONFIG_ERROR", message);
        }
        
        public ConfigException(String message, Throwable cause) {
            super("CONFIG_ERROR", message, cause);
        }
    }
    
    /**
     * 认证错误
     */
    public static class AuthException extends MTRWebCTCException {
        public AuthException(String message) {
            super("AUTH_ERROR", message);
        }
    }
    
    /**
     * 资源未找到
     */
    public static class NotFoundException extends MTRWebCTCException {
        public NotFoundException(String resource, long id) {
            super("NOT_FOUND", resource + " not found: " + id);
        }
        
        public NotFoundException(String resource, String id) {
            super("NOT_FOUND", resource + " not found: " + id);
        }
    }
    
    /**
     * 验证错误
     */
    public static class ValidationException extends MTRWebCTCException {
        public ValidationException(String message) {
            super("VALIDATION_ERROR", message);
        }
    }
    
    /**
     * 速率限制
     */
    public static class RateLimitException extends MTRWebCTCException {
        private final long retryAfterMs;
        
        public RateLimitException(long retryAfterMs) {
            super("RATE_LIMITED", "Too many requests. Retry after " + retryAfterMs + "ms");
            this.retryAfterMs = retryAfterMs;
        }
        
        public long getRetryAfterMs() {
            return retryAfterMs;
        }
    }
    
    /**
     * MTR 集成错误
     */
    public static class MTRException extends MTRWebCTCException {
        public MTRException(String message) {
            super("MTR_ERROR", message);
        }
        
        public MTRException(String message, Throwable cause) {
            super("MTR_ERROR", message, cause);
        }
    }
    
    /**
     * 备份错误
     */
    public static class BackupException extends MTRWebCTCException {
        public BackupException(String message) {
            super("BACKUP_ERROR", message);
        }
        
        public BackupException(String message, Throwable cause) {
            super("BACKUP_ERROR", message, cause);
        }
    }
}
