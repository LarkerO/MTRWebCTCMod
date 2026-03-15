package cn.bg7qvu.mtrwebctc.api;

import cn.bg7qvu.mtrwebctc.auth.AuthManager;
import cn.bg7qvu.mtrwebctc.backup.BackupManager;
import cn.bg7qvu.mtrwebctc.mtr.MTRDataManager;
import cn.bg7qvu.mtrwebctc.model.DepotDTO;
import cn.bg7qvu.mtrwebctc.model.TrainDTO;
import cn.bg7qvu.mtrwebctc.util.Logger;
import io.ktor.http.HttpStatusCode;
import io.ktor.server.application.*;
import io.ktor.server.request.receive;
import io.ktor.server.response.respond;
import io.ktor.server.routing.*;

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
    
    public Route.Routing.() -> Unit createRoutes() {
        return route -> {
            // GET /api/depots - 获取所有车厂
            route.get(ctx -> {
                try {
                    List<DepotDTO> depots = mtrDataManager.getAllDepots();
                    ctx.respond(depots);
                } catch (Exception e) {
                    Logger.error("Failed to get depots: " + e.getMessage());
                    ctx.respond(HttpStatusCode.InternalServerError, 
                                 new ErrorResponse("Failed to get depots: " + e.getMessage()));
                }
            });
            
            // GET /api/depots/{id} - 获取单个车厂
            route.get("{id}", ctx -> {
                try {
                    long id = Long.parseLong(ctx.pathParameters["id"]);
                    DepotDTO depot = mtrDataManager.getDepot(id);
                    if (depot != null) {
                        ctx.respond(depot);
                    } else {
                        ctx.respond(HttpStatusCode.NotFound, new ErrorResponse("Depot not found"));
                    }
                } catch (Exception e) {
                    ctx.respond(HttpStatusCode.BadRequest, new ErrorResponse("Invalid depot ID"));
                }
            });
            
            // PUT /api/depots/{id} - 修改车厂
            route.put("{id}", ctx -> {
                if (!authManager.validateRequest(ctx)) {
                    ctx.respond(HttpStatusCode.Unauthorized, new ErrorResponse("Unauthorized"));
                    return;
                }
                
                try {
                    long id = Long.parseLong(ctx.pathParameters["id"]);
                    DepotDTO depot = ctx.receive(DepotDTO.class);
                    depot.setId(id);
                    
                    // 创建备份
                    backupManager.createBackup("before-depot-update-" + id);
                    
                    boolean success = mtrDataManager.updateDepot(depot);
                    if (success) {
                        ctx.respond(depot);
                        Logger.info("Depot " + id + " updated successfully");
                    } else {
                        ctx.respond(HttpStatusCode.NotFound, new ErrorResponse("Depot not found"));
                    }
                } catch (Exception e) {
                    Logger.error("Failed to update depot: " + e.getMessage());
                    ctx.respond(HttpStatusCode.InternalServerError, 
                                 new ErrorResponse("Failed to update depot: " + e.getMessage()));
                }
            });
            
            // PUT /api/depots/{id}/schedule - 修改发车时间表
            route.put("{id}/schedule", ctx -> {
                if (!authManager.validateRequest(ctx)) {
                    ctx.respond(HttpStatusCode.Unauthorized, new ErrorResponse("Unauthorized"));
                    return;
                }
                
                try {
                    long id = Long.parseLong(ctx.pathParameters["id"]);
                    ScheduleUpdateRequest request = ctx.receive(ScheduleUpdateRequest.class);
                    
                    // 创建备份
                    backupManager.createBackup("before-schedule-update-" + id);
                    
                    boolean success = mtrDataManager.updateDepotSchedule(id, 
                        request.getDepartures(), request.getFrequencies(), 
                        request.isUseRealTime(), request.isRepeatInfinitely());
                    
                    if (success) {
                        ctx.respond(Map.of("success", true));
                        Logger.info("Depot " + id + " schedule updated successfully");
                    } else {
                        ctx.respond(HttpStatusCode.NotFound, new ErrorResponse("Depot not found"));
                    }
                } catch (Exception e) {
                    Logger.error("Failed to update schedule: " + e.getMessage());
                    ctx.respond(HttpStatusCode.InternalServerError, 
                                 new ErrorResponse("Failed to update schedule: " + e.getMessage()));
                }
            });
            
            // GET /api/depots/{id}/trains - 获取车厂内的列车
            route.get("{id}/trains", ctx -> {
                try {
                    long depotId = Long.parseLong(ctx.pathParameters["id"]);
                    List<TrainDTO> trains = mtrDataManager.getTrainsByDepot(depotId);
                    ctx.respond(trains);
                } catch (Exception e) {
                    Logger.error("Failed to get depot trains: " + e.getMessage());
                    ctx.respond(HttpStatusCode.InternalServerError, 
                                 new ErrorResponse("Failed to get depot trains: " + e.getMessage()));
                }
            });
        };
    }
    
    public static class ScheduleUpdateRequest {
        private List<Integer> departures;
        private List<Integer> frequencies;
        private boolean useRealTime;
        private boolean repeatInfinitely;
        
        public List<Integer> getDepartures() {
            return departures;
        }
        
        public void setDepartures(List<Integer> departures) {
            this.departures = departures;
        }
        
        public List<Integer> getFrequencies() {
            return frequencies;
        }
        
        public void setFrequencies(List<Integer> frequencies) {
            this.frequencies = frequencies;
        }
        
        public boolean isUseRealTime() {
            return useRealTime;
        }
        
        public void setUseRealTime(boolean useRealTime) {
            this.useRealTime = useRealTime;
        }
        
        public boolean isRepeatInfinitely() {
            return repeatInfinitely;
        }
        
        public void setRepeatInfinitely(boolean repeatInfinitely) {
            this.repeatInfinitely = repeatInfinitely;
        }
    }
    
    public static class ErrorResponse {
        private String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
        
        public String getError() {
            return error;
        }
    }
}
