package cn.bg7qvu.mtrwebctc.integration;

import cn.bg7qvu.mtrwebctc.MTRWebCTCMod;
import cn.bg7qvu.mtrwebctc.mtr.TrainTracker;
import cn.bg7qvu.mtrwebctc.websocket.WebSocketHandler;
import cn.bg7qvu.mtrwebctc.util.Logger;

import java.util.*;

/**
 * MTR 事件监听器
 * 监听 MTR 内部事件并触发相应处理
 */
public class MTREventListener {
    private final MTRWebCTCMod mod;
    private final TrainTracker trainTracker;
    private final WebSocketHandler wsHandler;
    
    // 事件监听器列表
    private final List<Object> registeredListeners = new ArrayList<>();
    
    public MTREventListener(MTRWebCTCMod mod, TrainTracker trainTracker, WebSocketHandler wsHandler) {
        this.mod = mod;
        this.trainTracker = trainTracker;
        this.wsHandler = wsHandler;
    }
    
    /**
     * 注册所有事件监听器
     */
    public void register() {
        Logger.info("Registering MTR event listeners...");
        
        // TODO: 实现实际的 MTR 事件监听
        // MTR 3.x 使用 Minecraft 事件系统
        // 需要根据 Fabric/Forge 分别实现
        
        Logger.info("MTR event listeners registered");
    }
    
    /**
     * 注销所有事件监听器
     */
    public void unregister() {
        Logger.info("Unregistering MTR event listeners...");
        
        // 清理监听器
        registeredListeners.clear();
        
        Logger.info("MTR event listeners unregistered");
    }
    
    // ========== 事件处理方法 ==========
    
    /**
     * 当列车生成时
     */
    public void onTrainSpawn(Object train) {
        Logger.debug("Train spawned: " + getTrainId(train));
        
        // 通知 WebSocket 客户端
        if (wsHandler != null) {
            wsHandler.broadcast("train_spawn", Map.of(
                "trainId", getTrainId(train),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
    
    /**
     * 当列车销毁时
     */
    public void onTrainDespawn(Object train) {
        Logger.debug("Train despawned: " + getTrainId(train));
        
        // 通知 WebSocket 客户端
        if (wsHandler != null) {
            wsHandler.broadcast("train_despawn", Map.of(
                "trainId", getTrainId(train),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
    
    /**
     * 当列车到达站台时
     */
    public void onTrainArrive(Object train, long stationId, long platformId) {
        Logger.debug("Train arrived at platform: " + platformId);
        
        // 记录到列车追踪器
        // trainTracker.recordArrival(getTrainId(train), stationId, platformId);
        
        // 通知 WebSocket 客户端
        if (wsHandler != null) {
            wsHandler.broadcast("train_arrive", Map.of(
                "trainId", getTrainId(train),
                "stationId", stationId,
                "platformId", platformId,
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
    
    /**
     * 当列车离开站台时
     */
    public void onTrainDepart(Object train, long stationId, long platformId) {
        Logger.debug("Train departed from platform: " + platformId);
        
        // 通知 WebSocket 客户端
        if (wsHandler != null) {
            wsHandler.broadcast("train_depart", Map.of(
                "trainId", getTrainId(train),
                "stationId", stationId,
                "platformId", platformId,
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
    
    /**
     * 当车站数据更新时
     */
    public void onStationUpdate(long stationId) {
        Logger.debug("Station updated: " + stationId);
        
        // 通知 WebSocket 客户端
        if (wsHandler != null) {
            wsHandler.broadcast("station_update", Map.of(
                "stationId", stationId,
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
    
    /**
     * 当线路数据更新时
     */
    public void onRouteUpdate(long routeId) {
        Logger.debug("Route updated: " + routeId);
        
        // 通知 WebSocket 客户端
        if (wsHandler != null) {
            wsHandler.broadcast("route_update", Map.of(
                "routeId", routeId,
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
    
    /**
     * 当车厂数据更新时
     */
    public void onDepotUpdate(long depotId) {
        Logger.debug("Depot updated: " + depotId);
        
        // 通知 WebSocket 客户端
        if (wsHandler != null) {
            wsHandler.broadcast("depot_update", Map.of(
                "depotId", depotId,
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
    
    // ========== 辅助方法 ==========
    
    private String getTrainId(Object train) {
        // TODO: 从 MTR Train 对象获取 ID
        return String.valueOf(System.identityHashCode(train));
    }
}
