package cn.bg7qvu.mtrwebctc.auth;

import cn.bg7qvu.mtrwebctc.config.Config;
import cn.bg7qvu.mtrwebctc.util.HashUtil;
import cn.bg7qvu.mtrwebctc.util.Logger;
import io.ktor.server.application.ApplicationCall;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Authentication manager
 */
public class AuthManager {
    private final Config config;
    private final Map<String, Long> tokens = new ConcurrentHashMap<>();
    private static final long TOKEN_EXPIRY_MS = 24 * 60 * 60 * 1000; // 24 hours

    public AuthManager(Config config) {
        this.config = config;
    }

    /**
     * Check if a password has been set
     */
    public boolean hasPassword() {
        return config.getAuth().getPasswordHash() != null &&
               !config.getAuth().getPasswordHash().isEmpty();
    }

    /**
     * Check if password authentication is required
     */
    public boolean isPasswordRequired() {
        return hasPassword();
    }

    /**
     * Set password (first time)
     */
    public void setPassword(String password) {
        String hash = HashUtil.hashPassword(password);
        config.getAuth().setPasswordHash(hash);

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
     * Login verification
     */
    public boolean login(String password) {
        if (!hasPassword()) {
            return true;
        }

        return HashUtil.verifyPassword(password, config.getAuth().getPasswordHash());
    }

    /**
     * Generate token
     */
    public String generateToken() {
        String token = HashUtil.generateToken();
        tokens.put(token, System.currentTimeMillis());
        return token;
    }

    /**
     * Validate token. Strips "Bearer " prefix if present.
     */
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        String actualToken = token;
        if (actualToken.startsWith("Bearer ")) {
            actualToken = actualToken.substring(7);
        }

        Long createdAt = tokens.get(actualToken);
        if (createdAt == null) {
            return false;
        }

        if (System.currentTimeMillis() - createdAt > TOKEN_EXPIRY_MS) {
            tokens.remove(actualToken);
            return false;
        }

        return true;
    }

    /**
     * Invalidate token
     */
    public void invalidateToken(String token) {
        tokens.remove(token);
    }

    /**
     * Validate request from a Ktor ApplicationCall by extracting the Authorization header.
     */
    public boolean validateRequest(ApplicationCall call) {
        String authHeader = call.getRequest().getHeaders().get("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }

        String token = authHeader.substring(7);
        return validateToken(token);
    }

    /**
     * Validate a request given a raw Authorization header value (e.g. "Bearer xxx").
     */
    public boolean validateRequest(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }

        String token = authHeader.substring(7);
        return validateToken(token);
    }

    /**
     * Whether WebAuthn is enabled
     */
    public boolean isWebauthnEnabled() {
        return config.getAuth().isWebauthnEnabled();
    }
}
