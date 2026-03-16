package cn.bg7qvu.mtrwebctc.api;

import cn.bg7qvu.mtrwebctc.auth.AuthManager;
import cn.bg7qvu.mtrwebctc.config.Config;
import cn.bg7qvu.mtrwebctc.config.ConfigLoader;
import cn.bg7qvu.mtrwebctc.util.Logger;
import io.ktor.http.HttpStatusCode;
import io.ktor.server.routing.Routing;
import io.ktor.server.routing.get;
import io.ktor.server.routing.post;
import io.ktor.server.routing.put;
import io.ktor.server.routing.route;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置 API 路由
 */
public class ConfigRouter {
    private final Config config;
    private final AuthManager authManager;
    
    public ConfigRouter(Config config, AuthManager authManager) {
        this.config = config;
        this.authManager = authManager;
    }
    
    private boolean checkAuth(io.ktor.server.routing.RoutingContext ctx) {
        String token = ctx.getCall().getRequest().getHeaders().get("Authorization");
        if (authManager.isPasswordRequired() && !authManager.validateToken(token)) {
            Map<String, String> result = new HashMap<>();
            result.put("error", "Unauthorized");
            ctx.getCall().respond(new HttpStatusCode(401, "Unauthorized"), result);
            return false;
        }
        return true;
    }
    
    public void register(Routing routing) {
        routing.route("/config", route -> {
            // GET /api/config - 获取配置
            route.get(ctx -> {
                if (!checkAuth(ctx)) return;
                
                // 返回安全的配置（不包含密码）
                Map<String, Object> safeConfig = new HashMap<>();
                Map<String, Object> server = new HashMap<>();
                server.put("port", config.getServer().getPort());
                server.put("bind", config.getServer().getBind());
                server.put("staticResourceMode", config.getServer().getStaticResourceMode());
                safeConfig.put("server", server);
                
                Map<String, Object> trainTracker = new HashMap<>();
                trainTracker.put("positionUpdateIntervalMs", config.getTrainTracker().getPositionUpdateIntervalMs());
                trainTracker.put("historyRetentionMinutes", config.getTrainTracker().getHistoryRetentionMinutes());
                safeConfig.put("trainTracker", trainTracker);
                
                Map<String, Object> websocket = new HashMap<>();
                websocket.put("pushIntervalMs", config.getWebsocket().getPushIntervalMs());
                safeConfig.put("websocket", websocket);
                
                Map<String, Object> backup = new HashMap<>();
                backup.put("enabled", config.getBackup().isEnabled());
                backup.put("maxBackups", config.getBackup().getMaxBackups());
                safeConfig.put("backup", backup);
                
                Map<String, Object> storage = new HashMap<>();
                storage.put("backend", config.getStorage().getBackend());
                safeConfig.put("storage", storage);
                
                Map<String, Object> auth = new HashMap<>();
                auth.put("webauthnEnabled", config.getAuth().isWebauthnEnabled());
                safeConfig.put("auth", auth);
                
                ctx.getCall().respond(safeConfig);
            });
            
            // PUT /api/config - 修改配置
            route.put(ctx -> {
                if (!checkAuth(ctx)) return;
                
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> body = ctx.getCall().receive(Map.class);
                    
                    // 更新配置
                    @SuppressWarnings("unchecked")
                    Map<String, Object> serverMap = (Map<String, Object>) body.get("server");
                    if (serverMap != null) {
                        Number port = (Number) serverMap.get("port");
                        if (port != null) {
                            if (port.intValue() < 1 || port.intValue() > 65535) {
                                Map<String, String> result = new HashMap<>();
                                result.put("error", "Invalid port");
                                ctx.getCall().respond(new HttpStatusCode(400, "Bad Request"), result);
                                return;
                            }
                            config.getServer().setPort(port.intValue());
                        }
                        String bind = (String) serverMap.get("bind");
                        if (bind != null) {
                            config.getServer().setBind(bind);
                        }
                    }
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> backupMap = (Map<String, Object>) body.get("backup");
                    if (backupMap != null) {
                        Boolean enabled = (Boolean) backupMap.get("enabled");
                        if (enabled != null) {
                            config.getBackup().setEnabled(enabled);
                        }
                        Number maxBackups = (Number) backupMap.get("maxBackups");
                        if (maxBackups != null) {
                            config.getBackup().setMaxBackups(maxBackups.intValue());
                        }
                    }
                    
                    // 保存配置
                    cn.bg7qvu.mtrwebctc.MTRWebCTCMod mod = 
                        cn.bg7qvu.mtrwebctc.MTRWebCTCMod.getInstance();
                    if (mod != null) {
                        ConfigLoader.save(mod.getConfigDir(), config);
                    }
                    
                    Map<String, Boolean> result = new HashMap<>();
                    result.put("success", true);
                    ctx.getCall().respond(result);
                    Logger.info("Configuration updated successfully");
                } catch (Exception e) {
                    Logger.error("Failed to update config: " + e.getMessage());
                    Map<String, String> result = new HashMap<>();
                    result.put("error", "Failed to update config");
                    ctx.getCall().respond(new HttpStatusCode(500, "Internal Server Error"), result);
                }
            });
            
            // POST /api/config/reload - 重载配置
            route.post("reload", ctx -> {
                if (!checkAuth(ctx)) return;
                
                try {
                    cn.bg7qvu.mtrwebctc.MTRWebCTCMod mod = 
                        cn.bg7qvu.mtrwebctc.MTRWebCTCMod.getInstance();
                    if (mod != null) {
                        ConfigLoader.reload(mod.getConfigDir());
                    }
                    Map<String, Boolean> result = new HashMap<>();
                    result.put("success", true);
                    ctx.getCall().respond(result);
                    Logger.info("Configuration reloaded successfully");
                } catch (Exception e) {
                    Logger.error("Failed to reload config: " + e.getMessage());
                    Map<String, String> result = new HashMap<>();
                    result.put("error", "Failed to reload config");
                    ctx.getCall().respond(new HttpStatusCode(500, "Internal Server Error"), result);
                }
            });
        });
    }
}
