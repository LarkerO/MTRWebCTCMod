package cn.bg7qvu.mtrwebctc.server;

import cn.bg7qvu.mtrwebctc.api.*;
import cn.bg7qvu.mtrwebctc.auth.AuthManager;
import cn.bg7qvu.mtrwebctc.backup.BackupManager;
import cn.bg7qvu.mtrwebctc.config.Config;
import cn.bg7qvu.mtrwebctc.mtr.MTRDataManager;
import cn.bg7qvu.mtrwebctc.mtr.TrainTracker;
import cn.bg7qvu.mtrwebctc.util.Logger;
import cn.bg7qvu.mtrwebctc.websocket.WebSocketHandler;
import io.ktor.http.ContentType;
import io.ktor.http.HttpStatusCode;
import io.ktor.serialization.gson.GsonConverter;
import io.ktor.server.application.Application;
import io.ktor.server.engine.ApplicationEngine;
import io.ktor.server.engine.embeddedServer;
import io.ktor.server.netty.Netty;
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation;
import io.ktor.server.plugins.cors.CORS;
import io.ktor.server.response.respond;
import io.ktor.server.response.respondText;
import io.ktor.server.routing.Route;
import io.ktor.server.routing.Routing;
import io.ktor.server.routing.get;
import io.ktor.server.routing.route;
import io.ktor.server.routing.routing;

import java.util.Collections;

/**
 * Ktor Web 服务器
 */
public class WebServer {
    private final Config config;
    private final MTRDataManager mtrDataManager;
    private final TrainTracker trainTracker;
    private final BackupManager backupManager;
    private final AuthManager authManager;
    private final WebSocketHandler webSocketHandler;
    
    private ApplicationEngine server;
    
    public WebServer(Config config, MTRDataManager mtrDataManager, 
                     TrainTracker trainTracker, BackupManager backupManager) {
        this.config = config;
        this.mtrDataManager = mtrDataManager;
        this.trainTracker = trainTracker;
        this.backupManager = backupManager;
        this.authManager = new AuthManager(config);
        this.webSocketHandler = new WebSocketHandler(config, mtrDataManager, trainTracker);
    }
    
    public void start() {
        Logger.info("Starting web server on " + config.getServer().getBind() + 
                    ":" + config.getServer().getPort());
        
        server = embeddedServer(Netty, 
            config.getServer().getPort(), 
            config.getServer().getBind(),
            this::configureApplication
        );
        
        server.start();
        Logger.info("Web server started successfully");
    }
    
    public void stop() {
        if (server != null) {
            Logger.info("Stopping web server...");
            webSocketHandler.stop();
            server.stop(1000, 5000);
            Logger.info("Web server stopped");
        }
    }
    
    private void configureApplication(Application app) {
        // 安装内容协商
        app.install(ContentNegotiation.class, plugin -> {
            plugin.register(ContentType.Application.Json, new GsonConverter());
        });
        
        // 安装 CORS
        app.install(CORS.class, plugin -> {
            plugin.anyHost();
            plugin.allowHeader("Authorization");
            plugin.allowHeader("Content-Type");
        });
        
        // 配置路由
        app.routing(this::configureRoutes);
    }
    
    private void configureRoutes(Routing routing) {
        // 根路径
        routing.get("/", ctx -> {
            ctx.respondText(
                "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>MTRWebCTC</title></head>" +
                "<body><h1>MTRWebCTC API</h1><p>Web interface coming soon...</p>" +
                "<h2>API Endpoints</h2><ul>" +
                "<li>POST /api/auth/login - Login</li>" +
                "<li>GET /api/stations - List stations</li>" +
                "<li>GET /api/routes - List routes</li>" +
                "<li>GET /api/depots - List depots</li>" +
                "<li>GET /api/trains - List trains</li>" +
                "</ul></body></html>",
                ContentType.Text.Html
            );
        });
        
        // 健康检查
        routing.get("/health", ctx -> {
            ctx.respond(Collections.singletonMap("status", "ok"));
        });
        
        // API 路由
        routing.route("/api", apiRoutes -> {
            // 认证
            new AuthRouter(authManager).register(apiRoutes);
            
            // 数据 API
            new StationRouter(mtrDataManager, authManager).register(apiRoutes);
            new RouteRouter(mtrDataManager, authManager).register(apiRoutes);
            new DepotRouter(mtrDataManager, authManager, backupManager).register(apiRoutes);
            new TrainRouter(trainTracker).register(apiRoutes);
            
            // 配置
            new ConfigRouter(config, authManager).register(apiRoutes);
            
            // 备份
            new BackupRouter(backupManager, authManager).register(apiRoutes);
            
            // 日志
            new LogRouter().register(apiRoutes);
        });
        
        // WebSocket
        webSocketHandler.register(routing);
    }
}
