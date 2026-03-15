package cn.bg7qvu.mtrwebctc.api;

import cn.bg7qvu.mtrwebctc.util.Constants;
import cn.bg7qvu.mtrwebctc.util.Logger;
import io.ktor.http.HttpStatusCode;
import io.ktor.server.routing.Routing;
import io.ktor.server.routing.get;
import io.ktor.server.routing.route;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统信息 API 路由
 */
public class SystemRouter {
    public void register(Routing routing) {
        routing.route("/system", route -> {
            // GET /api/system/info - 获取系统信息
            route.get("info", ctx -> {
                Map<String, Object> info = new HashMap<>();
                info.put("name", Constants.MOD_NAME);
                info.put("version", Constants.MOD_VERSION);
                info.put("java", System.getProperty("java.version"));
                info.put("os", System.getProperty("os.name"));
                info.put("arch", System.getProperty("os.arch"));
                
                // 运行时信息
                Runtime runtime = Runtime.getRuntime();
                info.put("maxMemory", runtime.maxMemory());
                info.put("totalMemory", runtime.totalMemory());
                info.put("freeMemory", runtime.freeMemory());
                info.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
                
                // 处理器数量
                info.put("processors", runtime.availableProcessors());
                
                // 时间戳
                info.put("timestamp", System.currentTimeMillis());
                info.put("uptime", getUptimeMs());
                
                ctx.getCall().respond(info);
            });
            
            // GET /api/system/health - 健康检查
            route.get("health", ctx -> {
                Map<String, Object> health = new HashMap<>();
                health.put("status", "healthy");
                health.put("timestamp", System.currentTimeMillis());
                
                // 内存状态
                Runtime runtime = Runtime.getRuntime();
                long usedMemory = runtime.totalMemory() - runtime.freeMemory();
                long maxMemory = runtime.maxMemory();
                double memoryUsage = (double) usedMemory / maxMemory * 100;
                
                health.put("memoryUsage", String.format("%.2f%%", memoryUsage));
                health.put("memoryHealthy", memoryUsage < 90);
                
                ctx.getCall().respond(health);
            });
            
            // GET /api/system/version - 版本信息
            route.get("version", ctx -> {
                Map<String, String> version = new HashMap<>();
                version.put("modVersion", Constants.MOD_VERSION);
                version.put("modName", Constants.MOD_NAME);
                version.put("modId", Constants.MOD_ID);
                
                ctx.getCall().respond(version);
            });
            
            // GET /api/system/stats - 统计信息
            route.get("stats", ctx -> {
                Map<String, Object> stats = new HashMap<>();
                
                Runtime runtime = Runtime.getRuntime();
                
                // 内存统计
                Map<String, Long> memory = new HashMap<>();
                memory.put("max", runtime.maxMemory());
                memory.put("total", runtime.totalMemory());
                memory.put("free", runtime.freeMemory());
                memory.put("used", runtime.totalMemory() - runtime.freeMemory());
                stats.put("memory", memory);
                
                // 线程统计
                Map<String, Integer> threads = new HashMap<>();
                threads.put("active", Thread.activeCount());
                threads.put("peak", Thread.activeCount()); // TODO: 跟踪峰值
                stats.put("threads", threads);
                
                stats.put("timestamp", System.currentTimeMillis());
                stats.put("uptimeMs", getUptimeMs());
                
                ctx.getCall().respond(stats);
            });
        });
    }
    
    private long getUptimeMs() {
        // TODO: 从 MTRWebCTCMod 获取实际启动时间
        return System.currentTimeMillis();
    }
}
