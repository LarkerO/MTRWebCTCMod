package cn.bg7qvu.mtrwebctc.integration;

import cn.bg7qvu.mtrwebctc.MTRWebCTCMod;
import cn.bg7qvu.mtrwebctc.mtr.TrainTracker;
import cn.bg7qvu.mtrwebctc.websocket.WebSocketHandler;
import cn.bg7qvu.mtrwebctc.util.Logger;

import java.util.*;

/**
 * MTR Event Listener
 * Listens to MTR internal events and triggers corresponding handlers.
 */
public class MTREventListener {
    private final MTRWebCTCMod mod;
    private final TrainTracker trainTracker;
    private final WebSocketHandler wsHandler;

    private final List<Object> registeredListeners = new ArrayList<>();

    public MTREventListener(MTRWebCTCMod mod, TrainTracker trainTracker, WebSocketHandler wsHandler) {
        this.mod = mod;
        this.trainTracker = trainTracker;
        this.wsHandler = wsHandler;
    }

    public void register() {
        Logger.info("Registering MTR event listeners...");
        // TODO: Implement actual MTR event listening per platform (Fabric/Forge)
        Logger.info("MTR event listeners registered");
    }

    public void unregister() {
        Logger.info("Unregistering MTR event listeners...");
        registeredListeners.clear();
        Logger.info("MTR event listeners unregistered");
    }

    // ========== Event handlers ==========

    public void onTrainSpawn(Object train) {
        Logger.debug("Train spawned: " + getTrainId(train));

        if (wsHandler != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("trainId", getTrainId(train));
            data.put("timestamp", System.currentTimeMillis());
            wsHandler.broadcast("train_spawn", data);
        }
    }

    public void onTrainDespawn(Object train) {
        Logger.debug("Train despawned: " + getTrainId(train));

        if (wsHandler != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("trainId", getTrainId(train));
            data.put("timestamp", System.currentTimeMillis());
            wsHandler.broadcast("train_despawn", data);
        }
    }

    public void onTrainArrive(Object train, long stationId, long platformId) {
        Logger.debug("Train arrived at platform: " + platformId);

        if (wsHandler != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("trainId", getTrainId(train));
            data.put("stationId", stationId);
            data.put("platformId", platformId);
            data.put("timestamp", System.currentTimeMillis());
            wsHandler.broadcast("train_arrive", data);
        }
    }

    public void onTrainDepart(Object train, long stationId, long platformId) {
        Logger.debug("Train departed from platform: " + platformId);

        if (wsHandler != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("trainId", getTrainId(train));
            data.put("stationId", stationId);
            data.put("platformId", platformId);
            data.put("timestamp", System.currentTimeMillis());
            wsHandler.broadcast("train_depart", data);
        }
    }

    public void onStationUpdate(long stationId) {
        Logger.debug("Station updated: " + stationId);

        if (wsHandler != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("stationId", stationId);
            data.put("timestamp", System.currentTimeMillis());
            wsHandler.broadcast("station_update", data);
        }
    }

    public void onRouteUpdate(long routeId) {
        Logger.debug("Route updated: " + routeId);

        if (wsHandler != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("routeId", routeId);
            data.put("timestamp", System.currentTimeMillis());
            wsHandler.broadcast("route_update", data);
        }
    }

    public void onDepotUpdate(long depotId) {
        Logger.debug("Depot updated: " + depotId);

        if (wsHandler != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("depotId", depotId);
            data.put("timestamp", System.currentTimeMillis());
            wsHandler.broadcast("depot_update", data);
        }
    }

    // ========== Helpers ==========

    private String getTrainId(Object train) {
        // TODO: Extract ID from MTR Train object
        return String.valueOf(System.identityHashCode(train));
    }
}
