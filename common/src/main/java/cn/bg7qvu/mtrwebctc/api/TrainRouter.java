package cn.bg7qvu.mtrwebctc.api;

import cn.bg7qvu.mtrwebctc.mtr.MTRDataManager;
import cn.bg7qvu.mtrwebctc.mtr.TrainTracker;
import cn.bg7qvu.mtrwebctc.model.TrainDTO;
import cn.bg7qvu.mtrwebctc.util.Logger;
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
    private final MTRDataManager mtrDataManager;
    private final TrainTracker trainTracker;
    
    public TrainRouter(MTRDataManager mtrDataManager, TrainTracker trainTracker) {
        this.mtrDataManager = mtrDataManager;
        this.trainTracker = trainTracker;
    }
    
    public void register(Routing routing) {
        routing.route("/trains", route -> {
            // GET /api/trains - 获取所有列车
            route.get(ctx -> {
                try {
                    List<TrainDTO> trains = mtrDataManager.getAllTrains();
                    ctx.getCall().respond(trains);
                } catch (Exception e) {
                    Logger.error("Failed to get trains: " + e.getMessage());
                    ctx.getCall().respond(HttpStatusCode.InternalServerError, 
                                 error("Failed to get trains: " + e.getMessage()));
                }
            });
            
            // GET /api/trains/{id} - 获取单个列车
            route.get("{id}", ctx -> {
                try {
                    String trainId = ctx.getCall().getParameters().get("id");
                    TrainDTO train = mtrDataManager.getTrain(trainId);
                    if (train != null) {
                        ctx.getCall().respond(train);
                    } else {
                        ctx.getCall().respond(HttpStatusCode.NotFound, error("Train not found"));
                    }
                } catch (Exception e) {
                    ctx.getCall().respond(HttpStatusCode.BadRequest, error("Invalid train ID"));
                }
            });
            
            // GET /api/trains/{id}/history - 获取列车历史轨迹
            route.get("{id}/history", ctx -> {
                try {
                    String trainId = ctx.getCall().getParameters().get("id");
                    List<TrainDTO.Position> history = trainTracker.getTrainHistory(trainId);
                    ctx.getCall().respond(history);
                } catch (Exception e) {
                    Logger.error("Failed to get train history: " + e.getMessage());
                    ctx.getCall().respond(HttpStatusCode.InternalServerError, 
                                 error("Failed to get train history: " + e.getMessage()));
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
