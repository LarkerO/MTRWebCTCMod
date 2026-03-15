package cn.bg7qvu.mtrwebctc.api;

import cn.bg7qvu.mtrwebctc.auth.AuthManager;
import cn.bg7qvu.mtrwebctc.mtr.MTRDataManager;
import cn.bg7qvu.mtrwebctc.model.RouteDTO;
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
 * 线路 API 路由
 */
public class RouteRouter {
    private final MTRDataManager mtrDataManager;
    private final AuthManager authManager;
    
    public RouteRouter(MTRDataManager mtrDataManager, AuthManager authManager) {
        this.mtrDataManager = mtrDataManager;
        this.authManager = authManager;
    }
    
    public void register(Routing routing) {
        routing.route("/routes", route -> {
            // GET /api/routes - 获取所有线路
            route.get(ctx -> {
                try {
                    List<RouteDTO> routes = mtrDataManager.getAllRoutes();
                    ctx.getCall().respond(routes);
                } catch (Exception e) {
                    Logger.error("Failed to get routes: " + e.getMessage());
                    ctx.getCall().respond(HttpStatusCode.InternalServerError, 
                                 error("Failed to get routes: " + e.getMessage()));
                }
            });
            
            // GET /api/routes/{id} - 获取单个线路
            route.get("{id}", ctx -> {
                try {
                    long id = Long.parseLong(ctx.getCall().getParameters().get("id"));
                    RouteDTO route = mtrDataManager.getRoute(id);
                    if (route != null) {
                        ctx.getCall().respond(route);
                    } else {
                        ctx.getCall().respond(HttpStatusCode.NotFound, error("Route not found"));
                    }
                } catch (Exception e) {
                    ctx.getCall().respond(HttpStatusCode.BadRequest, error("Invalid route ID"));
                }
            });
            
            // PUT /api/routes/{id} - 修改线路
            route.put("{id}", ctx -> {
                if (!authManager.validateRequest(ctx.getCall().getApplicationCall())) {
                    ctx.getCall().respond(HttpStatusCode.Unauthorized, error("Unauthorized"));
                    return;
                }
                
                try {
                    long id = Long.parseLong(ctx.getCall().getParameters().get("id"));
                    RouteDTO route = ctx.getCall().receive(RouteDTO.class);
                    route.setId(id);
                    
                    boolean success = mtrDataManager.updateRoute(route);
                    if (success) {
                        ctx.getCall().respond(route);
                        Logger.info("Route " + id + " updated successfully");
                    } else {
                        ctx.getCall().respond(HttpStatusCode.NotFound, error("Route not found"));
                    }
                } catch (Exception e) {
                    Logger.error("Failed to update route: " + e.getMessage());
                    ctx.getCall().respond(HttpStatusCode.InternalServerError, 
                                 error("Failed to update route: " + e.getMessage()));
                }
            });
            
            // GET /api/routes/{id}/trains - 获取线路上的列车
            route.get("{id}/trains", ctx -> {
                try {
                    long routeId = Long.parseLong(ctx.getCall().getParameters().get("id"));
                    List<TrainDTO> trains = mtrDataManager.getTrainsByRoute(routeId);
                    ctx.getCall().respond(trains);
                } catch (Exception e) {
                    Logger.error("Failed to get trains: " + e.getMessage());
                    ctx.getCall().respond(HttpStatusCode.InternalServerError, 
                                 error("Failed to get trains: " + e.getMessage()));
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
