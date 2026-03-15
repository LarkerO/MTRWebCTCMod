package cn.bg7qvu.mtrwebctc.auth;

import cn.bg7qvu.mtrwebctc.config.Config;
import cn.bg7qvu.mtrwebctc.util.HashUtil;
import cn.bg7qvu.mtrwebctc.util.Logger;
import io.ktor.server.application.ApplicationCall;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 认证管理器
 */
public class AuthManager {
    private final Config config;
    private final Map<String, Long> tokens = new ConcurrentHashMap<>();
    private static final long TOKEN_EXPIRY_MS = 24 * 60 * 60 * 1000; // 24 小时
    
    public AuthManager(Config config) {
        this.config = config;
    }
    
    /**
     * 检查是否已设置密码
     */
    public boolean hasPassword() {
        return config.getAuth().getPasswordHash() != null && 
               !config.getAuth().getPasswordHash().isEmpty();
    }
    
    /**
     * 设置密码（首次）
     */
    public void setPassword(String password) {
        String hash = HashUtil.hashPassword(password);
        config.getAuth().setPasswordHash(hash);
        
        // 保存配置
        try {
            cn.bg7qvu.mtrwebctc.config.ConfigLoader.save(
                cn.bg7qvu.mtrwebctc.MTRWebCTCMod.getInstance().getConfigDir(), 
                config
            );
        } catch (Exception e) {
            Logger.error("Failed to save password: " + e.getMessage());
        }
    }
    
    /**
     * 登录验证
     */
    public boolean login(String password) {
        if (!hasPassword()) {
            // 未设置密码，允许任何密码登录（首次设置）
            return true;
        }
        
        return HashUtil.verifyPassword(password, config.getAuth().getPasswordHash());
    }
    
    /**
     * 生成 token
     */
    public String generateToken() {
        String token = HashUtil.generateToken();
        tokens.put(token, System.currentTimeMillis());
        return token;
    }
    
    /**
     * 验证 token
     */
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        Long createdAt = tokens.get(token);
        if (createdAt == null) {
            return false;
        }
        
        // 检查是否过期
        if (System.currentTimeMillis() - createdAt > TOKEN_EXPIRY_MS) {
            tokens.remove(token);
            return false;
        }
        
        return true;
    }
    
    /**
     * 使 token 失效
     */
    public void invalidateToken(String token) {
        tokens.remove(token);
    }
    
    /**
     * 从请求中验证
     */
    public boolean validateRequest(ApplicationCall call) {
        String authHeader = call.request.headers["Authorization"];
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        
        String token = authHeader.substring(7);
        return validateToken(token);
    }
    
    /**
     * WebAuthn 是否启用
     */
    public boolean isWebauthnEnabled() {
        return config.getAuth().isWebauthnEnabled();
    }
}
