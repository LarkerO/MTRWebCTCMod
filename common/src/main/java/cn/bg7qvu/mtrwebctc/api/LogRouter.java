package cn.bg7qvu.mtrwebctc.api;

import io.ktor.server.routing.*;

/**
 * 日志 API 路由
 */
public class LogRouter {
    
    public Route.Routing.() -> Unit createRoutes() {
        return route -> {
            // GET /api/logs - 获取操作日志
            route.get(ctx -> {
                // TODO: 实现日志读取
                ctx.respond(java.util.Collections.emptyList());
            });
        };
    }
}
