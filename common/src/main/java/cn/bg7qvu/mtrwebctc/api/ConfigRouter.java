package cn.bg7qvu.mtrwebctc.api;

import cn.bg7qvu.mtrwebctc.config.Config;
import cn.bg7qvu.mtrwebctc.config.ConfigLoader;
import cn.bg7qvu.mtrwebctc.util.Logger;
import io.ktor.http.HttpStatusCode;
import io.ktor.server.application.*;
import io.ktor.server.request.receive;
import io.ktor.server.response.respond;
import io.ktor.server.routing.*;

import java.nio.file.Path;
import java.util.Map;

/**
 * 配置 API 路由
 */
public class ConfigRouter {
    private final Config config;
    
    public ConfigRouter(Config config) {
        this.config = config;
    }
    
    public Route.Routing.() -> Unit createRoutes() {
        return route -> {
            // GET /api/config - 获取配置
            route.get(ctx -> {
                ctx.respond(config);
            });
            
            // PUT /api/config - 修改配置
            route.put(ctx -> {
                try {
                    Config newConfig = ctx.receive(Config.class);
                    
                    // 验证配置
                    if (newConfig.getServer().getPort() < 1 || 
                        newConfig.getServer().getPort() > 65535) {
                        ctx.respond(HttpStatusCode.BadRequest, 
                                     Map.of("error", "Invalid port"));
                        return;
                    }
                    
                    // 更新配置
                    config.setServer(newConfig.getServer());
                    config.setTrainTracker(newConfig.getTrainTracker());
                    config.setWebsocket(newConfig.getWebsocket());
                    config.setBackup(newConfig.getBackup());
                    config.setStorage(newConfig.getStorage());
                    
                    // 保存配置
                    Path configDir = cn.bg7qvu.mtrwebctc.MTRWebCTCMod.getInstance().getConfigDir();
                    ConfigLoader.save(configDir, config);
                    
                    ctx.respond(config);
                    Logger.info("Configuration updated successfully");
                } catch (Exception e) {
                    Logger.error("Failed to update config: " + e.getMessage());
                    ctx.respond(HttpStatusCode.InternalServerError, 
                                 Map.of("error", "Failed to update config"));
                }
            });
            
            // POST /api/config/reload - 重载配置
            route.post("reload", ctx -> {
                try {
                    Path configDir = cn.bg7qvu.mtrwebctc.MTRWebCTCMod.getInstance().getConfigDir();
                    ConfigLoader.reload(configDir);
                    ctx.respond(Map.of("success", true));
                    Logger.info("Configuration reloaded successfully");
                } catch (Exception e) {
                    Logger.error("Failed to reload config: " + e.getMessage());
                    ctx.respond(HttpStatusCode.InternalServerError, 
                                 Map.of("error", "Failed to reload config"));
                }
            });
        };
    }
}
