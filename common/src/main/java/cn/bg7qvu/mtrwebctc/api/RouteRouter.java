package cn.bg7qvu.mtrwebctc.api;

import cn.bg7qvu.mtrwebctc.auth.AuthManager;
import cn.bg7qvu.mtrwebctc.mtr.MTRDataManager;
import cn.bg7qvu.mtrwebctc.model.RouteDTO;
import cn.bg7qvu.mtrwebctc.model.TrainDTO;
import cn.bg7qvu.mtrwebctc.util.Logger;
import io.ktor.http.HttpStatusCode;
import io.ktor.server.application.*;
import io.ktor.server.request.receive;
import io.ktor.server.response.respond;
import io.ktor.server.routing.*;

import java.util.List;

/**
 * 线路 API 路由
 */
public class RouteRouter {
    private final MTRDataManager mtrDataManager;
    private final AuthManager authManager;
    
    public RouteRouter(MTRDataManager mtrDataManager, AuthManager authManager) {
        this.mtrDataManager = mtrDataManager;
        this.authManager = authManager;
    }
    
    public Route.Routing.() -> Unit createRoutes() {
        return route -> {
            // GET /api/routes - 获取所有线路
            route.get(ctx -> {
                try {
                    List<RouteDTO> routes = mtrDataManager.getAllRoutes();
                    ctx.respond(routes);
                } catch (Exception e) {
                    Logger.error("Failed to get routes: " + e.getMessage());
                    ctx.respond(HttpStatusCode.InternalServerError, 
                                 new ErrorResponse("Failed to get routes: " + e.getMessage()));
                }
            });
            
            // GET /api/routes/{id} - 获取单个线路
            route.get("{id}", ctx -> {
                try {
                    long id = Long.parseLong(ctx.pathParameters["id"]);
                    RouteDTO route = mtrDataManager.getRoute(id);
                    if (route != null) {
                        ctx.respond(route);
                    } else {
                        ctx.respond(HttpStatusCode.NotFound, new ErrorResponse("Route not found"));
                    }
                } catch (Exception e) {
                    ctx.respond(HttpStatusCode.BadRequest, new ErrorResponse("Invalid route ID"));
                }
            });
            
            // PUT /api/routes/{id} - 修改线路
            route.put("{id}", ctx -> {
                if (!authManager.validateRequest(ctx)) {
                    ctx.respond(HttpStatusCode.Unauthorized, new ErrorResponse("Unauthorized"));
                    return;
                }
                
                try {
                    long id = Long.parseLong(ctx.pathParameters["id"]);
                    RouteDTO route = ctx.receive(RouteDTO.class);
                    route.setId(id);
                    
                    boolean success = mtrDataManager.updateRoute(route);
                    if (success) {
                        ctx.respond(route);
                        Logger.info("Route " + id + " updated successfully");
                    } else {
                        ctx.respond(HttpStatusCode.NotFound, new ErrorResponse("Route not found"));
                    }
                } catch (Exception e) {
                    Logger.error("Failed to update route: " + e.getMessage());
                    ctx.respond(HttpStatusCode.InternalServerError, 
                                 new ErrorResponse("Failed to update route: " + e.getMessage()));
                }
            });
            
            // GET /api/routes/{id}/trains - 获取线路上的列车
            route.get("{id}/trains", ctx -> {
                try {
                    long routeId = Long.parseLong(ctx.pathParameters["id"]);
                    List<TrainDTO> trains = mtrDataManager.getTrainsByRoute(routeId);
                    ctx.respond(trains);
                } catch (Exception e) {
                    Logger.error("Failed to get trains: " + e.getMessage());
                    ctx.respond(HttpStatusCode.InternalServerError, 
                                 new ErrorResponse("Failed to get trains: " + e.getMessage()));
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
