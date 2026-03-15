package cn.bg7qvu.mtrwebctc.api;

import io.ktor.server.routing.Routing;
import io.ktor.server.routing.get;
import io.ktor.server.routing.route;

import java.util.Collections;

/**
 * 日志 API 路由
 */
public class LogRouter {
    
    public void register(Routing routing) {
        routing.route("/logs", route -> {
            // GET /api/logs - 获取操作日志
            route.get(ctx -> {
                // TODO: 实现日志读取
                ctx.getCall().respond(Collections.emptyList());
            });
        });
    }
}
