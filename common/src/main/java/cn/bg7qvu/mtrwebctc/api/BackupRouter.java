package cn.bg7qvu.mtrwebctc.api;

import cn.bg7qvu.mtrwebctc.auth.AuthManager;
import cn.bg7qvu.mtrwebctc.backup.BackupManager;
import cn.bg7qvu.mtrwebctc.util.Logger;
import io.ktor.http.HttpStatusCode;
import io.ktor.server.routing.Routing;
import io.ktor.server.routing.delete;
import io.ktor.server.routing.get;
import io.ktor.server.routing.post;
import io.ktor.server.routing.route;

import java.util.HashMap;
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
    
    public void register(Routing routing) {
        routing.route("/backups", route -> {
            // GET /api/backups - 获取备份列表
            route.get(ctx -> {
                try {
                    List<BackupManager.BackupInfo> backups = backupManager.getBackupList();
                    ctx.getCall().respond(backups);
                } catch (Exception e) {
                    Logger.error("Failed to get backups: " + e.getMessage());
                    Map<String, String> result = new HashMap<>();
                    result.put("error", "Failed to get backups");
                    ctx.getCall().respond(HttpStatusCode.InternalServerError, result);
                }
            });
            
            // POST /api/backups - 创建备份
            route.post(ctx -> {
                if (!authManager.validateRequest(ctx.getCall().getApplicationCall())) {
                    Map<String, String> result = new HashMap<>();
                    result.put("error", "Unauthorized");
                    ctx.getCall().respond(HttpStatusCode.Unauthorized, result);
                    return;
                }
                
                try {
                    String backupId = backupManager.createBackup("manual");
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("backupId", backupId);
                    ctx.getCall().respond(result);
                    Logger.info("Manual backup created: " + backupId);
                } catch (Exception e) {
                    Logger.error("Failed to create backup: " + e.getMessage());
                    Map<String, String> result = new HashMap<>();
                    result.put("error", "Failed to create backup");
                    ctx.getCall().respond(HttpStatusCode.InternalServerError, result);
                }
            });
            
            // POST /api/backups/{id}/restore - 恢复备份
            route.post("{id}/restore", ctx -> {
                if (!authManager.validateRequest(ctx.getCall().getApplicationCall())) {
                    Map<String, String> result = new HashMap<>();
                    result.put("error", "Unauthorized");
                    ctx.getCall().respond(HttpStatusCode.Unauthorized, result);
                    return;
                }
                
                try {
                    String backupId = ctx.getCall().getParameters().get("id");
                    boolean success = backupManager.restoreBackup(backupId);
                    if (success) {
                        Map<String, Boolean> result = new HashMap<>();
                        result.put("success", true);
                        ctx.getCall().respond(result);
                        Logger.info("Backup restored: " + backupId);
                    } else {
                        Map<String, String> result = new HashMap<>();
                        result.put("error", "Backup not found");
                        ctx.getCall().respond(HttpStatusCode.NotFound, result);
                    }
                } catch (Exception e) {
                    Logger.error("Failed to restore backup: " + e.getMessage());
                    Map<String, String> result = new HashMap<>();
                    result.put("error", "Failed to restore backup");
                    ctx.getCall().respond(HttpStatusCode.InternalServerError, result);
                }
            });
            
            // DELETE /api/backups/{id} - 删除备份
            route.delete("{id}", ctx -> {
                if (!authManager.validateRequest(ctx.getCall().getApplicationCall())) {
                    Map<String, String> result = new HashMap<>();
                    result.put("error", "Unauthorized");
                    ctx.getCall().respond(HttpStatusCode.Unauthorized, result);
                    return;
                }
                
                try {
                    String backupId = ctx.getCall().getParameters().get("id");
                    boolean success = backupManager.deleteBackup(backupId);
                    if (success) {
                        Map<String, Boolean> result = new HashMap<>();
                        result.put("success", true);
                        ctx.getCall().respond(result);
                        Logger.info("Backup deleted: " + backupId);
                    } else {
                        Map<String, String> result = new HashMap<>();
                        result.put("error", "Backup not found");
                        ctx.getCall().respond(HttpStatusCode.NotFound, result);
                    }
                } catch (Exception e) {
                    Logger.error("Failed to delete backup: " + e.getMessage());
                    Map<String, String> result = new HashMap<>();
                    result.put("error", "Failed to delete backup");
                    ctx.getCall().respond(HttpStatusCode.InternalServerError, result);
                }
            });
        });
    }
}
