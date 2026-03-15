package cn.bg7qvu.mtrwebctc.security;

import cn.bg7qvu.mtrwebctc.util.Logger;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * 令牌生成器
 */
public final class TokenGenerator {
    private TokenGenerator() {}
    
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int DEFAULT_TOKEN_LENGTH = 32;
    
    /**
     * 生成随机令牌
     * @return Base64 编码的令牌
     */
    public static String generate() {
        return generate(DEFAULT_TOKEN_LENGTH);
    }
    
    /**
     * 生成指定长度的随机令牌
     * @param byteLength 字节长度
     * @return Base64 编码的令牌
     */
    public static String generate(int byteLength) {
        byte[] bytes = new byte[byteLength];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    /**
     * 生成十六进制令牌
     * @param byteLength 字节长度
     * @return 十六进制字符串
     */
    public static String generateHex(int byteLength) {
        byte[] bytes = new byte[byteLength];
        RANDOM.nextBytes(bytes);
        
        StringBuilder sb = new StringBuilder(byteLength * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        
        return sb.toString();
    }
    
    /**
     * 生成数字验证码
     * @param digits 位数
     * @return 数字验证码
     */
    public static String generateNumeric(int digits) {
        StringBuilder sb = new StringBuilder(digits);
        for (int i = 0; i < digits; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
    
    /**
     * 生成 API Key
     * @return API Key (格式: pk_xxx)
     */
    public static String generateApiKey() {
        return "pk_" + generate(24);
    }
    
    /**
     * 生成会话 ID
     * @return 会话 ID (格式: sess_xxx)
     */
    public static String generateSessionId() {
        return "sess_" + generate(16);
    }
    
    /**
     * 验证令牌格式
     * @param token 令牌
     * @return 是否有效
     */
    public static boolean isValid(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        try {
            Base64.getUrlDecoder().decode(token);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 生成盐值
     * @return Base64 编码的盐值
     */
    public static String generateSalt() {
        return generate(16);
    }
}
