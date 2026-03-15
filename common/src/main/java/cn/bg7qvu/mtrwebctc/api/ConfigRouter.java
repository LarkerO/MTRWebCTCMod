package cn.bg7qvu.mtrwebctc.api;

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
    
    public ConfigRouter(Config config) {
        this.config = config;
    }
    
    public void register(Routing routing) {
        routing.route("/config", route -> {
            // GET /api/config - 获取配置
            route.get(ctx -> {
                ctx.getCall().respond(config);
            });
            
            // PUT /api/config - 修改配置
            route.put(ctx -> {
                try {
                    Config newConfig = ctx.getCall().receive(Config.class);
                    
                    // 验证配置
                    if (newConfig.getServer().getPort() < 1 || 
                        newConfig.getServer().getPort() > 65535) {
                        Map<String, String> result = new HashMap<>();
                        result.put("error", "Invalid port");
                        ctx.getCall().respond(HttpStatusCode.BadRequest, result);
                        return;
                    }
                    
                    // 更新配置
                    config.setServer(newConfig.getServer());
                    config.setTrainTracker(newConfig.getTrainTracker());
                    config.setWebsocket(newConfig.getWebsocket());
                    config.setBackup(newConfig.getBackup());
                    config.setStorage(newConfig.getStorage());
                    
                    // 保存配置
                    cn.bg7qvu.mtrwebctc.MTRWebCTCMod mod = 
                        cn.bg7qvu.mtrwebctc.MTRWebCTCMod.getInstance();
                    if (mod != null) {
                        ConfigLoader.save(mod.getConfigDir(), config);
                    }
                    
                    ctx.getCall().respond(config);
                    Logger.info("Configuration updated successfully");
                } catch (Exception e) {
                    Logger.error("Failed to update config: " + e.getMessage());
                    Map<String, String> result = new HashMap<>();
                    result.put("error", "Failed to update config");
                    ctx.getCall().respond(HttpStatusCode.InternalServerError, result);
                }
            });
            
            // POST /api/config/reload - 重载配置
            route.post("reload", ctx -> {
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
                    ctx.getCall().respond(HttpStatusCode.InternalServerError, result);
                }
            });
        });
    }
}
