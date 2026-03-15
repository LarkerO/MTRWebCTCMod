package cn.bg7qvu.mtrwebctc.util;

/**
 * 验证工具类
 */
public final class ValidationUtil {
    private ValidationUtil() {}
    
    /**
     * 验证非空字符串
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
    
    /**
     * 验证空字符串
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * 验证端口号
     */
    public static boolean isValidPort(int port) {
        return port > 0 && port <= 65535;
    }
    
    /**
     * 验证正整数
     */
    public static boolean isPositive(long value) {
        return value > 0;
    }
    
    /**
     * 验证非负数
     */
    public static boolean isNonNegative(long value) {
        return value >= 0;
    }
    
    /**
     * 验证范围
     */
    public static boolean isInRange(long value, long min, long max) {
        return value >= min && value <= max;
    }
    
    /**
     * 验证范围
     */
    public static boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }
    
    /**
     * 验证 ID（非负整数）
     */
    public static boolean isValidId(long id) {
        return id >= 0;
    }
    
    /**
     * 验证坐标
     */
    public static boolean isValidCoordinate(double coord) {
        return Double.isFinite(coord) && Math.abs(coord) <= 30000000;
    }
    
    /**
     * 验证 UUID 格式
     */
    public static boolean isValidUuid(String uuid) {
        if (isEmpty(uuid)) {
            return false;
        }
        
        // 标准 UUID 格式: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
        return uuid.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }
    
    /**
     * 验证十六进制颜色
     */
    public static boolean isValidHexColor(String color) {
        if (isEmpty(color)) {
            return false;
        }
        
        return color.matches("^#[0-9a-fA-F]{6}$") || color.matches("^#[0-9a-fA-F]{8}$");
    }
    
    /**
     * 验证 URL
     */
    public static boolean isValidUrl(String url) {
        if (isEmpty(url)) {
            return false;
        }
        
        return url.matches("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$");
    }
    
    /**
     * 验证 JSON 字符串（简单检查）
     */
    public static boolean isValidJson(String json) {
        if (isEmpty(json)) {
            return false;
        }
        
        String trimmed = json.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
               (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }
    
    /**
     * 验证令牌格式
     */
    public static boolean isValidToken(String token) {
        if (isEmpty(token)) {
            return false;
        }
        
        // 令牌应该是字母数字字符串，长度至少 16
        return token.length() >= 16 && token.matches("^[a-zA-Z0-9_-]+$");
    }
    
    /**
     * 验证密码强度
     * @param password 密码
     * @param minLength 最小长度
     * @return 是否满足要求
     */
    public static boolean isStrongPassword(String password, int minLength) {
        if (isEmpty(password)) {
            return false;
        }
        
        return password.length() >= minLength;
    }
    
    /**
     * 限制值在范围内
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * 限制值在范围内
     */
    public static long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * 限制值在范围内
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
