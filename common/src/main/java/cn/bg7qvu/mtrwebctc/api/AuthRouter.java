package cn.bg7qvu.mtrwebctc.api;

import cn.bg7qvu.mtrwebctc.auth.AuthManager;
import cn.bg7qvu.mtrwebctc.util.HashUtil;
import cn.bg7qvu.mtrwebctc.util.Logger;
import io.ktor.http.HttpStatusCode;
import io.ktor.server.application.*;
import io.ktor.server.request.receive;
import io.ktor.server.response.respond;
import io.ktor.server.routing.*;

import java.util.Map;

/**
 * 认证 API 路由
 */
public class AuthRouter {
    private final AuthManager authManager;
    
    public AuthRouter(AuthManager authManager) {
        this.authManager = authManager;
    }
    
    public Route.Routing.() -> Unit createRoutes() {
        return route -> {
            // POST /api/auth/login - 登录
            route.post("login", ctx -> {
                try {
                    LoginRequest request = ctx.receive(LoginRequest.class);
                    
                    if (authManager.login(request.getPassword())) {
                        String token = authManager.generateToken();
                        ctx.respond(Map.of("token", token, "success", true));
                        Logger.info("User logged in successfully");
                    } else {
                        ctx.respond(HttpStatusCode.Unauthorized, 
                                     Map.of("success", false, "error", "Invalid password"));
                    }
                } catch (Exception e) {
                    Logger.error("Login failed: " + e.getMessage());
                    ctx.respond(HttpStatusCode.InternalServerError, 
                                 Map.of("success", false, "error", "Login failed"));
                }
            });
            
            // POST /api/auth/logout - 登出
            route.post("logout", ctx -> {
                String token = ctx.request.headers["Authorization"];
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);
                    authManager.invalidateToken(token);
                }
                ctx.respond(Map.of("success", true));
            });
            
            // POST /api/auth/password - 设置密码（首次）
            route.post("password", ctx -> {
                try {
                    PasswordRequest request = ctx.receive(PasswordRequest.class);
                    
                    if (authManager.hasPassword()) {
                        ctx.respond(HttpStatusCode.BadRequest, 
                                     Map.of("success", false, "error", "Password already set"));
                        return;
                    }
                    
                    authManager.setPassword(request.getPassword());
                    ctx.respond(Map.of("success", true));
                    Logger.info("Password set successfully");
                } catch (Exception e) {
                    Logger.error("Failed to set password: " + e.getMessage());
                    ctx.respond(HttpStatusCode.InternalServerError, 
                                 Map.of("success", false, "error", "Failed to set password"));
                }
            });
            
            // WebAuthn 相关（简化实现）
            route.get("webauthn/register", ctx -> {
                if (!authManager.isWebauthnEnabled()) {
                    ctx.respond(HttpStatusCode.BadRequest, Map.of("error", "WebAuthn not enabled"));
                    return;
                }
                // TODO: 实现 WebAuthn 注册挑战
                ctx.respond(Map.of("challenge", HashUtil.generateToken()));
            });
            
            route.post("webauthn/verify", ctx -> {
                if (!authManager.isWebauthnEnabled()) {
                    ctx.respond(HttpStatusCode.BadRequest, Map.of("error", "WebAuthn not enabled"));
                    return;
                }
                // TODO: 实现 WebAuthn 验证
                ctx.respond(Map.of("success", true));
            });
        };
    }
    
    public static class LoginRequest {
        private String password;
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
    }
    
    public static class PasswordRequest {
        private String password;
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
    }
}
