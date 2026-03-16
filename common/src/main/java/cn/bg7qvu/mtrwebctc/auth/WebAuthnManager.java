package cn.bg7qvu.mtrwebctc.auth;

import cn.bg7qvu.mtrwebctc.util.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebAuthn 认证管理器
 * 注：当前为桩实现，WebAuthn 需要前端配合
 */
public class WebAuthnManager {
    // 注册中的挑战码
    private final Map<String, Challenge> registrationChallenges = new ConcurrentHashMap<>();
    
    // 认证中的挑战码
    private final Map<String, Challenge> authenticationChallenges = new ConcurrentHashMap<>();
    
    // 已注册的凭证
    private final Map<String, WebAuthnCredential> credentials = new ConcurrentHashMap<>();
    
    // Relying Party 信息
    private static final String RP_ID = "localhost";
    private static final String RP_NAME = "MTRWebCTC";
    
    /**
     * 生成注册挑战码
     */
    public Map<String, Object> generateRegistrationChallenge(String username) {
        String challengeId = UUID.randomUUID().toString();
        byte[] challenge = generateRandomBytes(32);
        
        registrationChallenges.put(challengeId, new Challenge(challengeId, challenge, username));
        
        // 返回 PublicKeyCredentialCreationOptions
        Map<String, Object> options = new LinkedHashMap<>();
        options.put("rp", createMap("id", RP_ID, "name", RP_NAME));
        options.put("user", createMap(
            "id", Base64.getEncoder().encodeToString(username.getBytes()),
            "name", username,
            "displayName", username
        ));
        options.put("challenge", Base64.getUrlEncoder().withoutPadding().encodeToString(challenge));
        options.put("pubKeyCredParams", Arrays.asList(
            createMap("type", "public-key", "alg", -7),   // ES256
            createMap("type", "public-key", "alg", -257)  // RS256
        ));
        options.put("timeout", 60000);
        options.put("challengeId", challengeId);
        
        Logger.debug("Generated registration challenge for: " + username);
        return options;
    }
    
    /**
     * 验证注册响应
     */
    public boolean verifyRegistration(String challengeId, Map<String, Object> response) {
        Challenge challenge = registrationChallenges.remove(challengeId);
        if (challenge == null) {
            Logger.warn("Invalid registration challenge: " + challengeId);
            return false;
        }
        
        // 检查是否过期
        if (challenge.isExpired()) {
            Logger.warn("Registration challenge expired: " + challengeId);
            return false;
        }
        
        // TODO: 实际验证 WebAuthn 响应
        // 需要验证:
        // 1. clientDataJSON
        // 2. attestationObject
        // 3. challenge 匹配
        
        // 模拟成功
        String credentialId = (String) response.get("id");
        String username = challenge.username;
        
        credentials.put(credentialId, new WebAuthnCredential(
            credentialId,
            username,
            (String) response.get("type"),
            System.currentTimeMillis()
        ));
        
        Logger.info("WebAuthn credential registered for: " + username);
        return true;
    }
    
    /**
     * 生成认证挑战码
     */
    public Map<String, Object> generateAuthenticationChallenge(String username) {
        String challengeId = UUID.randomUUID().toString();
        byte[] challenge = generateRandomBytes(32);
        
        authenticationChallenges.put(challengeId, new Challenge(challengeId, challenge, username));
        
        // 查找用户的凭证
        List<Map<String, Object>> allowCredentials = new ArrayList<>();
        for (WebAuthnCredential cred : credentials.values()) {
            if (cred.username.equals(username)) {
                allowCredentials.add(createMap(
                    "type", "public-key",
                    "id", cred.credentialId,
                    "transports", Arrays.asList("internal", "usb", "nfc", "ble")
                ));
            }
        }
        
        // 返回 PublicKeyCredentialRequestOptions
        Map<String, Object> options = new LinkedHashMap<>();
        options.put("challenge", Base64.getUrlEncoder().withoutPadding().encodeToString(challenge));
        options.put("timeout", 60000);
        options.put("rpId", RP_ID);
        options.put("allowCredentials", allowCredentials);
        options.put("userVerification", "preferred");
        options.put("challengeId", challengeId);
        
        Logger.debug("Generated authentication challenge for: " + username);
        return options;
    }
    
    /**
     * 验证认证响应
     */
    public boolean verifyAuthentication(String challengeId, Map<String, Object> response) {
        Challenge challenge = authenticationChallenges.remove(challengeId);
        if (challenge == null) {
            Logger.warn("Invalid authentication challenge: " + challengeId);
            return false;
        }
        
        // 检查是否过期
        if (challenge.isExpired()) {
            Logger.warn("Authentication challenge expired: " + challengeId);
            return false;
        }
        
        // TODO: 实际验证 WebAuthn 响应
        // 需要验证:
        // 1. clientDataJSON
        // 2. authenticatorData
        // 3. signature
        
        String credentialId = (String) response.get("id");
        WebAuthnCredential cred = credentials.get(credentialId);
        
        if (cred == null) {
            Logger.warn("Unknown credential: " + credentialId);
            return false;
        }
        
        Logger.info("WebAuthn authentication successful for: " + cred.username);
        return true;
    }
    
    /**
     * 检查用户是否已注册 WebAuthn
     */
    public boolean hasCredential(String username) {
        return credentials.values().stream()
            .anyMatch(c -> c.username.equals(username));
    }
    
    /**
     * 移除凭证
     */
    public boolean removeCredential(String credentialId) {
        WebAuthnCredential removed = credentials.remove(credentialId);
        return removed != null;
    }
    
    /**
     * 获取用户的凭证列表
     */
    public List<Map<String, Object>> getUserCredentials(String username) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (WebAuthnCredential cred : credentials.values()) {
            if (cred.username.equals(username)) {
                result.add(createMap(
                    "id", cred.credentialId,
                    "type", cred.type,
                    "createdAt", cred.createdAt
                ));
            }
        }
        return result;
    }
    
    // 辅助方法
    private byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        new Random().nextBytes(bytes);
        return bytes;
    }
    
    // 内部类
    private static class Challenge {
        final String id;
        final byte[] challenge;
        final String username;
        final long createdAt;
        
        Challenge(String id, byte[] challenge, String username) {
            this.id = id;
            this.challenge = challenge;
            this.username = username;
            this.createdAt = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - createdAt > 60000; // 1分钟过期
        }
    }
    
    private static class WebAuthnCredential {
        final String credentialId;
        final String username;
        final String type;
        final long createdAt;
        
        WebAuthnCredential(String credentialId, String username, String type, long createdAt) {
            this.credentialId = credentialId;
            this.username = username;
            this.type = type;
            this.createdAt = createdAt;
        }
    }
    
    @SuppressWarnings("unchecked")
    private static <K, V> Map<K, V> createMap(Object... keyValues) {
        Map<K, V> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((K) keyValues[i], (V) keyValues[i + 1]);
        }
        return map;
    }
}
