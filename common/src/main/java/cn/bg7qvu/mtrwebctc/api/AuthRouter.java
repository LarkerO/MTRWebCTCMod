package cn.bg7qvu.mtrwebctc.api;

import cn.bg7qvu.mtrwebctc.auth.AuthManager;
import cn.bg7qvu.mtrwebctc.util.HashUtil;
import cn.bg7qvu.mtrwebctc.util.Logger;
import io.ktor.http.HttpStatusCode;
import io.ktor.server.routing.Routing;
import io.ktor.server.routing.get;
import io.ktor.server.routing.post;
import io.ktor.server.routing.route;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证 API 路由
 */
public class AuthRouter {
    private final AuthManager authManager;
    
    public AuthRouter(AuthManager authManager) {
        this.authManager = authManager;
    }
    
    public void register(Routing routing) {
        routing.route("/auth", route -> {
            // POST /api/auth/login - 登录
            route.post("login", ctx -> {
                try {
                    LoginRequest request = ctx.getCall().receive(LoginRequest.class);
                    
                    if (authManager.login(request.getPassword())) {
                        String token = authManager.generateToken();
                        Map<String, Object> result = new HashMap<>();
                        result.put("token", token);
                        result.put("success", true);
                        ctx.getCall().respond(result);
                        Logger.info("User logged in successfully");
                    } else {
                        Map<String, Object> result = new HashMap<>();
                        result.put("success", false);
                        result.put("error", "Invalid password");
                        ctx.getCall().respond(HttpStatusCode.Unauthorized, result);
                    }
                } catch (Exception e) {
                    Logger.error("Login failed: " + e.getMessage());
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", false);
                    result.put("error", "Login failed");
                    ctx.getCall().respond(HttpStatusCode.InternalServerError, result);
                }
            });
            
            // POST /api/auth/logout - 登出
            route.post("logout", ctx -> {
                String token = ctx.getCall().getRequest().getHeaders().get("Authorization");
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);
                    authManager.invalidateToken(token);
                }
                Map<String, Boolean> result = new HashMap<>();
                result.put("success", true);
                ctx.getCall().respond(result);
            });
            
            // POST /api/auth/password - 设置密码（首次）
            route.post("password", ctx -> {
                try {
                    PasswordRequest request = ctx.getCall().receive(PasswordRequest.class);
                    
                    if (authManager.hasPassword()) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("success", false);
                        result.put("error", "Password already set");
                        ctx.getCall().respond(HttpStatusCode.BadRequest, result);
                        return;
                    }
                    
                    authManager.setPassword(request.getPassword());
                    Map<String, Boolean> result = new HashMap<>();
                    result.put("success", true);
                    ctx.getCall().respond(result);
                    Logger.info("Password set successfully");
                } catch (Exception e) {
                    Logger.error("Failed to set password: " + e.getMessage());
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", false);
                    result.put("error", "Failed to set password");
                    ctx.getCall().respond(HttpStatusCode.InternalServerError, result);
                }
            });
            
            // WebAuthn 相关
            route.get("webauthn/register", ctx -> {
                if (!authManager.isWebauthnEnabled()) {
                    Map<String, String> result = new HashMap<>();
                    result.put("error", "WebAuthn not enabled");
                    ctx.getCall().respond(HttpStatusCode.BadRequest, result);
                    return;
                }
                Map<String, String> result = new HashMap<>();
                result.put("challenge", HashUtil.generateToken());
                ctx.getCall().respond(result);
            });
            
            route.post("webauthn/verify", ctx -> {
                if (!authManager.isWebauthnEnabled()) {
                    Map<String, String> result = new HashMap<>();
                    result.put("error", "WebAuthn not enabled");
                    ctx.getCall().respond(HttpStatusCode.BadRequest, result);
                    return;
                }
                Map<String, Boolean> result = new HashMap<>();
                result.put("success", true);
                ctx.getCall().respond(result);
            });
        });
    }
    
    public static class LoginRequest {
        private String password;
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class PasswordRequest {
        private String password;
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
