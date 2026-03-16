package cn.bg7qvu.mtrwebctc.api;

import cn.bg7qvu.mtrwebctc.mtr.TrainTracker;
import cn.bg7qvu.mtrwebctc.model.TrainDTO;
import cn.bg7qvu.mtrwebctc.util.Logger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.ktor.http.HttpStatusCode;
import io.ktor.server.routing.Routing;
import io.ktor.server.routing.get;
import io.ktor.server.routing.route;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 列车 API 路由
 */
public class TrainRouter {
    private final TrainTracker trainTracker;
    private final Gson gson = new Gson();
    
    public TrainRouter(TrainTracker trainTracker) {
        this.trainTracker = trainTracker;
    }
    
    public void register(Routing routing) {
        routing.route("/trains", route -> {
            // GET /api/trains - 获取所有列车
            route.get(ctx -> {
                try {
                    List<TrainDTO> trains = trainTracker.getAllTrains();
                    Map<String, Object> response = new HashMap<>();
                    response.put("trains", trains);
                    response.put("count", trains.size());
                    response.put("timestamp", System.currentTimeMillis());
                    ctx.getCall().respond(response);
                } catch (Exception e) {
                    Logger.error("Failed to get trains: " + e.getMessage());
                    ctx.getCall().respond(new HttpStatusCode(500, "Internal Server Error"), 
                                 error("Failed to get trains: " + e.getMessage()));
                }
            });
            
            // GET /api/trains/{id} - 获取单个列车
            route.get("{id}", ctx -> {
                try {
                    String trainId = ctx.getCall().getParameters().get("id");
                    if (trainId == null || trainId.isEmpty()) {
                        ctx.getCall().respond(new HttpStatusCode(400, "Bad Request"), error("Missing train ID"));
                        return;
                    }
                    
                    TrainDTO train = trainTracker.getTrain(trainId);
                    if (train != null) {
                        ctx.getCall().respond(train);
                    } else {
                        ctx.getCall().respond(new HttpStatusCode(404, "Not Found"), error("Train not found: " + trainId));
                    }
                } catch (Exception e) {
                    Logger.error("Failed to get train: " + e.getMessage());
                    ctx.getCall().respond(new HttpStatusCode(500, "Internal Server Error"), error("Failed to get train"));
                }
            });
            
            // GET /api/trains/{id}/history - 获取列车历史轨迹
            route.get("{id}/history", ctx -> {
                try {
                    String trainId = ctx.getCall().getParameters().get("id");
                    if (trainId == null || trainId.isEmpty()) {
                        ctx.getCall().respond(new HttpStatusCode(400, "Bad Request"), error("Missing train ID"));
                        return;
                    }
                    
                    List<TrainDTO.Position> history = trainTracker.getTrainHistory(trainId);
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("trainId", trainId);
                    response.put("positions", history);
                    response.put("count", history.size());
                    response.put("timestamp", System.currentTimeMillis());
                    
                    ctx.getCall().respond(response);
                } catch (Exception e) {
                    Logger.error("Failed to get train history: " + e.getMessage());
                    ctx.getCall().respond(new HttpStatusCode(500, "Internal Server Error"), 
                                 error("Failed to get train history: " + e.getMessage()));
                }
            });
            
            // GET /api/trains/route/{routeId} - 获取线路上的列车
            route.get("route/{routeId}", ctx -> {
                try {
                    String routeIdStr = ctx.getCall().getParameters().get("routeId");
                    if (routeIdStr == null || routeIdStr.isEmpty()) {
                        ctx.getCall().respond(new HttpStatusCode(400, "Bad Request"), error("Missing route ID"));
                        return;
                    }
                    
                    long routeId = Long.parseLong(routeIdStr);
                    List<TrainDTO> trains = trainTracker.getRouteTrains(routeId);
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("routeId", routeId);
                    response.put("trains", trains);
                    response.put("count", trains.size());
                    response.put("timestamp", System.currentTimeMillis());
                    
                    ctx.getCall().respond(response);
                } catch (NumberFormatException e) {
                    ctx.getCall().respond(new HttpStatusCode(400, "Bad Request"), error("Invalid route ID"));
                } catch (Exception e) {
                    Logger.error("Failed to get route trains: " + e.getMessage());
                    ctx.getCall().respond(new HttpStatusCode(500, "Internal Server Error"), error("Failed to get route trains"));
                }
            });
            
            // GET /api/trains/depot/{depotId} - 获取车厂内的列车
            route.get("depot/{depotId}", ctx -> {
                try {
                    String depotIdStr = ctx.getCall().getParameters().get("depotId");
                    if (depotIdStr == null || depotIdStr.isEmpty()) {
                        ctx.getCall().respond(new HttpStatusCode(400, "Bad Request"), error("Missing depot ID"));
                        return;
                    }
                    
                    long depotId = Long.parseLong(depotIdStr);
                    List<TrainDTO> trains = trainTracker.getDepotTrains(depotId);
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("depotId", depotId);
                    response.put("trains", trains);
                    response.put("count", trains.size());
                    response.put("timestamp", System.currentTimeMillis());
                    
                    ctx.getCall().respond(response);
                } catch (NumberFormatException e) {
                    ctx.getCall().respond(new HttpStatusCode(400, "Bad Request"), error("Invalid depot ID"));
                } catch (Exception e) {
                    Logger.error("Failed to get depot trains: " + e.getMessage());
                    ctx.getCall().respond(new HttpStatusCode(500, "Internal Server Error"), error("Failed to get depot trains"));
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
