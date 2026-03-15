package cn.bg7qvu.mtrwebctc.api;

import cn.bg7qvu.mtrwebctc.auth.AuthManager;
import cn.bg7qvu.mtrwebctc.mtr.MTRDataManager;
import cn.bg7qvu.mtrwebctc.model.StationDTO;
import cn.bg7qvu.mtrwebctc.util.Logger;
import io.ktor.http.HttpStatusCode;
import io.ktor.server.routing.Route;
import io.ktor.server.routing.Routing;
import io.ktor.server.routing.get;
import io.ktor.server.routing.put;
import io.ktor.server.routing.route;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 车站 API 路由
 */
public class StationRouter {
    private final MTRDataManager mtrDataManager;
    private final AuthManager authManager;
    
    public StationRouter(MTRDataManager mtrDataManager, AuthManager authManager) {
        this.mtrDataManager = mtrDataManager;
        this.authManager = authManager;
    }
    
    public void register(Routing routing) {
        routing.route("/stations", route -> {
            // GET /api/stations - 获取所有车站
            route.get(ctx -> {
                try {
                    List<StationDTO> stations = mtrDataManager.getAllStations();
                    ctx.getCall().respond(stations);
                } catch (Exception e) {
                    Logger.error("Failed to get stations: " + e.getMessage());
                    ctx.getCall().respond(HttpStatusCode.InternalServerError, 
                                 error("Failed to get stations: " + e.getMessage()));
                }
            });
            
            // GET /api/stations/{id} - 获取单个车站
            route.get("{id}", ctx -> {
                try {
                    long id = Long.parseLong(ctx.getCall().getParameters().get("id"));
                    StationDTO station = mtrDataManager.getStation(id);
                    if (station != null) {
                        ctx.getCall().respond(station);
                    } else {
                        ctx.getCall().respond(HttpStatusCode.NotFound, error("Station not found"));
                    }
                } catch (NumberFormatException e) {
                    ctx.getCall().respond(HttpStatusCode.BadRequest, error("Invalid station ID"));
                } catch (Exception e) {
                    Logger.error("Failed to get station: " + e.getMessage());
                    ctx.getCall().respond(HttpStatusCode.InternalServerError, 
                                 error("Failed to get station: " + e.getMessage()));
                }
            });
            
            // PUT /api/stations/{id} - 修改车站
            route.put("{id}", ctx -> {
                // 验证权限
                if (!authManager.validateRequest(ctx.getCall().getApplicationCall())) {
                    ctx.getCall().respond(HttpStatusCode.Unauthorized, error("Unauthorized"));
                    return;
                }
                
                try {
                    long id = Long.parseLong(ctx.getCall().getParameters().get("id"));
                    StationDTO station = ctx.getCall().receive(StationDTO.class);
                    station.setId(id);
                    
                    boolean success = mtrDataManager.updateStation(station);
                    if (success) {
                        ctx.getCall().respond(station);
                        Logger.info("Station " + id + " updated successfully");
                    } else {
                        ctx.getCall().respond(HttpStatusCode.NotFound, error("Station not found"));
                    }
                } catch (NumberFormatException e) {
                    ctx.getCall().respond(HttpStatusCode.BadRequest, error("Invalid station ID"));
                } catch (Exception e) {
                    Logger.error("Failed to update station: " + e.getMessage());
                    ctx.getCall().respond(HttpStatusCode.InternalServerError, 
                                 error("Failed to update station: " + e.getMessage()));
                }
            });
            
            // GET /api/stations/{id}/platforms - 获取车站的站台
            route.get("{id}/platforms", ctx -> {
                try {
                    long stationId = Long.parseLong(ctx.getCall().getParameters().get("id"));
                    ctx.getCall().respond(mtrDataManager.getPlatformsByStation(stationId));
                } catch (Exception e) {
                    Logger.error("Failed to get platforms: " + e.getMessage());
                    ctx.getCall().respond(HttpStatusCode.InternalServerError, 
                                 error("Failed to get platforms: " + e.getMessage()));
                }
            });
        });
    }
    
    private Map<String, String> error(String message) {
        Map<String, String> result = new HashMap<>();
        result.put("error", message);
        return result;
    }
}
