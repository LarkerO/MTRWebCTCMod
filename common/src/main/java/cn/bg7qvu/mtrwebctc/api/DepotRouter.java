package cn.bg7qvu.mtrwebctc.api;

import cn.bg7qvu.mtrwebctc.auth.AuthManager;
import cn.bg7qvu.mtrwebctc.backup.BackupManager;
import cn.bg7qvu.mtrwebctc.mtr.MTRDataManager;
import cn.bg7qvu.mtrwebctc.model.DepotDTO;
import cn.bg7qvu.mtrwebctc.model.TrainDTO;
import cn.bg7qvu.mtrwebctc.util.Logger;
import io.ktor.http.HttpStatusCode;
import io.ktor.server.routing.Routing;
import io.ktor.server.routing.get;
import io.ktor.server.routing.put;
import io.ktor.server.routing.route;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 车厂 API 路由
 */
public class DepotRouter {
    private final MTRDataManager mtrDataManager;
    private final AuthManager authManager;
    private final BackupManager backupManager;
    
    public DepotRouter(MTRDataManager mtrDataManager, AuthManager authManager, 
                       BackupManager backupManager) {
        this.mtrDataManager = mtrDataManager;
        this.authManager = authManager;
        this.backupManager = backupManager;
    }
    
    public void register(Routing routing) {
        routing.route("/depots", route -> {
            // GET /api/depots - 获取所有车厂
            route.get(ctx -> {
                try {
                    List<DepotDTO> depots = mtrDataManager.getAllDepots();
                    ctx.getCall().respond(depots);
                } catch (Exception e) {
                    Logger.error("Failed to get depots: " + e.getMessage());
                    ctx.getCall().respond(new HttpStatusCode(500, "Internal Server Error"), 
                                 error("Failed to get depots: " + e.getMessage()));
                }
            });
            
            // GET /api/depots/{id} - 获取单个车厂
            route.get("{id}", ctx -> {
                try {
                    long id = Long.parseLong(ctx.getCall().getParameters().get("id"));
                    DepotDTO depot = mtrDataManager.getDepot(id);
                    if (depot != null) {
                        ctx.getCall().respond(depot);
                    } else {
                        ctx.getCall().respond(new HttpStatusCode(404, "Not Found"), error("Depot not found"));
                    }
                } catch (Exception e) {
                    ctx.getCall().respond(new HttpStatusCode(400, "Bad Request"), error("Invalid depot ID"));
                }
            });
            
            // PUT /api/depots/{id} - 修改车厂
            route.put("{id}", ctx -> {
                if (!authManager.validateRequest(ctx.getCall().getApplicationCall())) {
                    ctx.getCall().respond(new HttpStatusCode(401, "Unauthorized"), error("Unauthorized"));
                    return;
                }
                
                try {
                    long id = Long.parseLong(ctx.getCall().getParameters().get("id"));
                    DepotDTO depot = ctx.getCall().receive(DepotDTO.class);
                    depot.setId(id);
                    
                    // 创建备份
                    backupManager.createBackup("before-depot-update-" + id);
                    
                    boolean success = mtrDataManager.updateDepot(depot);
                    if (success) {
                        ctx.getCall().respond(depot);
                        Logger.info("Depot " + id + " updated successfully");
                    } else {
                        ctx.getCall().respond(new HttpStatusCode(404, "Not Found"), error("Depot not found"));
                    }
                } catch (Exception e) {
                    Logger.error("Failed to update depot: " + e.getMessage());
                    ctx.getCall().respond(new HttpStatusCode(500, "Internal Server Error"), 
                                 error("Failed to update depot: " + e.getMessage()));
                }
            });
            
            // PUT /api/depots/{id}/schedule - 修改发车时间表
            route.put("{id}/schedule", ctx -> {
                if (!authManager.validateRequest(ctx.getCall().getApplicationCall())) {
                    ctx.getCall().respond(new HttpStatusCode(401, "Unauthorized"), error("Unauthorized"));
                    return;
                }
                
                try {
                    long id = Long.parseLong(ctx.getCall().getParameters().get("id"));
                    ScheduleUpdateRequest request = ctx.getCall().receive(ScheduleUpdateRequest.class);
                    
                    // 创建备份
                    backupManager.createBackup("before-schedule-update-" + id);
                    
                    boolean success = mtrDataManager.updateDepotSchedule(id, 
                        request.getDepartures(), request.getFrequencies(), 
                        request.isUseRealTime(), request.isRepeatInfinitely());
                    
                    if (success) {
                        ctx.getCall().respond(successMap());
                        Logger.info("Depot " + id + " schedule updated successfully");
                    } else {
                        ctx.getCall().respond(new HttpStatusCode(404, "Not Found"), error("Depot not found"));
                    }
                } catch (Exception e) {
                    Logger.error("Failed to update schedule: " + e.getMessage());
                    ctx.getCall().respond(new HttpStatusCode(500, "Internal Server Error"), 
                                 error("Failed to update schedule: " + e.getMessage()));
                }
            });
            
            // GET /api/depots/{id}/trains - 获取车厂内的列车
            route.get("{id}/trains", ctx -> {
                try {
                    long depotId = Long.parseLong(ctx.getCall().getParameters().get("id"));
                    List<TrainDTO> trains = mtrDataManager.getTrainsByDepot(depotId);
                    ctx.getCall().respond(trains);
                } catch (Exception e) {
                    Logger.error("Failed to get depot trains: " + e.getMessage());
                    ctx.getCall().respond(new HttpStatusCode(500, "Internal Server Error"), 
                                 error("Failed to get depot trains: " + e.getMessage()));
                }
            });
        });
    }
    
    private Map<String, String> error(String message) {
        Map<String, String> result = new HashMap<>();
        result.put("error", message);
        return result;
    }
    
    private Map<String, Boolean> successMap() {
        Map<String, Boolean> result = new HashMap<>();
        result.put("success", true);
        return result;
    }
    
    public static class ScheduleUpdateRequest {
        private List<Integer> departures;
        private List<Integer> frequencies;
        private boolean useRealTime;
        private boolean repeatInfinitely;
        
        public List<Integer> getDepartures() { return departures; }
        public void setDepartures(List<Integer> departures) { this.departures = departures; }
        public List<Integer> getFrequencies() { return frequencies; }
        public void setFrequencies(List<Integer> frequencies) { this.frequencies = frequencies; }
        public boolean isUseRealTime() { return useRealTime; }
        public void setUseRealTime(boolean useRealTime) { this.useRealTime = useRealTime; }
        public boolean isRepeatInfinitely() { return repeatInfinitely; }
        public void setRepeatInfinitely(boolean repeatInfinitely) { this.repeatInfinitely = repeatInfinitely; }
    }
}
