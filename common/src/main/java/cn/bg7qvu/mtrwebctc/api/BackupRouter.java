package cn.bg7qvu.mtrwebctc.api;

import cn.bg7qvu.mtrwebctc.auth.AuthManager;
import cn.bg7qvu.mtrwebctc.backup.BackupManager;
import cn.bg7qvu.mtrwebctc.util.Logger;
import io.ktor.http.HttpStatusCode;
import io.ktor.server.application.*;
import io.ktor.server.response.respond;
import io.ktor.server.routing.*;

import java.util.List;
import java.util.Map;

/**
 * 备份 API 路由
 */
public class BackupRouter {
    private final BackupManager backupManager;
    private final AuthManager authManager;
    
    public BackupRouter(BackupManager backupManager, AuthManager authManager) {
        this.backupManager = backupManager;
        this.authManager = authManager;
    }
    
    public Route.Routing.() -> Unit createRoutes() {
        return route -> {
            // GET /api/backups - 获取备份列表
            route.get(ctx -> {
                try {
                    List<BackupManager.BackupInfo> backups = backupManager.getBackupList();
                    ctx.respond(backups);
                } catch (Exception e) {
                    Logger.error("Failed to get backups: " + e.getMessage());
                    ctx.respond(HttpStatusCode.InternalServerError, 
                                 Map.of("error", "Failed to get backups"));
                }
            });
            
            // POST /api/backups - 创建备份
            route.post(ctx -> {
                if (!authManager.validateRequest(ctx)) {
                    ctx.respond(HttpStatusCode.Unauthorized, Map.of("error", "Unauthorized"));
                    return;
                }
                
                try {
                    String backupId = backupManager.createBackup("manual");
                    ctx.respond(Map.of("success", true, "backupId", backupId));
                    Logger.info("Manual backup created: " + backupId);
                } catch (Exception e) {
                    Logger.error("Failed to create backup: " + e.getMessage());
                    ctx.respond(HttpStatusCode.InternalServerError, 
                                 Map.of("error", "Failed to create backup"));
                }
            });
            
            // POST /api/backups/{id}/restore - 恢复备份
            route.post("{id}/restore", ctx -> {
                if (!authManager.validateRequest(ctx)) {
                    ctx.respond(HttpStatusCode.Unauthorized, Map.of("error", "Unauthorized"));
                    return;
                }
                
                try {
                    String backupId = ctx.pathParameters["id"];
                    boolean success = backupManager.restoreBackup(backupId);
                    if (success) {
                        ctx.respond(Map.of("success", true));
                        Logger.info("Backup restored: " + backupId);
                    } else {
                        ctx.respond(HttpStatusCode.NotFound, Map.of("error", "Backup not found"));
                    }
                } catch (Exception e) {
                    Logger.error("Failed to restore backup: " + e.getMessage());
                    ctx.respond(HttpStatusCode.InternalServerError, 
                                 Map.of("error", "Failed to restore backup"));
                }
            });
            
            // DELETE /api/backups/{id} - 删除备份
            route.delete("{id}", ctx -> {
                if (!authManager.validateRequest(ctx)) {
                    ctx.respond(HttpStatusCode.Unauthorized, Map.of("error", "Unauthorized"));
                    return;
                }
                
                try {
                    String backupId = ctx.pathParameters["id"];
                    boolean success = backupManager.deleteBackup(backupId);
                    if (success) {
                        ctx.respond(Map.of("success", true));
                        Logger.info("Backup deleted: " + backupId);
                    } else {
                        ctx.respond(HttpStatusCode.NotFound, Map.of("error", "Backup not found"));
                    }
                } catch (Exception e) {
                    Logger.error("Failed to delete backup: " + e.getMessage());
                    ctx.respond(HttpStatusCode.InternalServerError, 
                                 Map.of("error", "Failed to delete backup"));
                }
            });
        };
    }
}
