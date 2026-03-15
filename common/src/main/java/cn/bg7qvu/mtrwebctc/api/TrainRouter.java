package cn.bg7qvu.mtrwebctc.api;

import cn.bg7qvu.mtrwebctc.mtr.MTRDataManager;
import cn.bg7qvu.mtrwebctc.mtr.TrainTracker;
import cn.bg7qvu.mtrwebctc.model.TrainDTO;
import cn.bg7qvu.mtrwebctc.util.Logger;
import io.ktor.http.HttpStatusCode;
import io.ktor.server.application.*;
import io.ktor.server.response.respond;
import io.ktor.server.routing.*;

import java.util.List;

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
    
    public Route.Routing.() -> Unit createRoutes() {
        return route -> {
            // GET /api/trains - 获取所有列车
            route.get(ctx -> {
                try {
                    List<TrainDTO> trains = mtrDataManager.getAllTrains();
                    ctx.respond(trains);
                } catch (Exception e) {
                    Logger.error("Failed to get trains: " + e.getMessage());
                    ctx.respond(HttpStatusCode.InternalServerError, 
                                 new ErrorResponse("Failed to get trains: " + e.getMessage()));
                }
            });
            
            // GET /api/trains/{id} - 获取单个列车
            route.get("{id}", ctx -> {
                try {
                    String trainId = ctx.pathParameters["id"];
                    TrainDTO train = mtrDataManager.getTrain(trainId);
                    if (train != null) {
                        ctx.respond(train);
                    } else {
                        ctx.respond(HttpStatusCode.NotFound, new ErrorResponse("Train not found"));
                    }
                } catch (Exception e) {
                    ctx.respond(HttpStatusCode.BadRequest, new ErrorResponse("Invalid train ID"));
                }
            });
            
            // GET /api/trains/{id}/history - 获取列车历史轨迹
            route.get("{id}/history", ctx -> {
                try {
                    String trainId = ctx.pathParameters["id"];
                    List<TrainDTO.Position> history = trainTracker.getTrainHistory(trainId);
                    ctx.respond(history);
                } catch (Exception e) {
                    Logger.error("Failed to get train history: " + e.getMessage());
                    ctx.respond(HttpStatusCode.InternalServerError, 
                                 new ErrorResponse("Failed to get train history: " + e.getMessage()));
                }
            });
        };
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
