package cn.bg7qvu.mtrwebctc.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 哈希工具类
 */
public class HashUtil {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SALT_LENGTH = 16;
    
    /**
     * 生成随机盐值
     */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * 使用 SHA-256 哈希密码
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String combined = password + salt;
            byte[] hash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            return "sha256:" + salt + ":" + Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
    
    /**
     * 哈希密码（自动生成盐值）
     */
    public static String hashPassword(String password) {
        String salt = generateSalt();
        return hashPassword(password, salt);
    }
    
    /**
     * 验证密码
     */
    public static boolean verifyPassword(String password, String storedHash) {
        if (storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        
        String[] parts = storedHash.split(":");
        if (parts.length != 3 || !"sha256".equals(parts[0])) {
            return false;
        }
        
        String salt = parts[1];
        String computedHash = hashPassword(password, salt);
        return storedHash.equals(computedHash);
    }
    
    /**
     * 生成随机 token
     */
    public static String generateToken() {
        byte[] token = new byte[32];
        RANDOM.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }
}
