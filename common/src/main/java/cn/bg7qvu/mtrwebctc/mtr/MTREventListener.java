package cn.bg7qvu.mtrwebctc.mtr;

import cn.bg7qvu.mtrwebctc.backup.BackupManager;
import cn.bg7qvu.mtrwebctc.util.Logger;

/**
 * MTR 事件监听器
 * 监听 MTR 相关事件并触发相应操作
 */
public class MTREventListener {
    private final MTRDataManager mtrDataManager;
    private final TrainTracker trainTracker;
    private final BackupManager backupManager;
    
    public MTREventListener(MTRDataManager mtrDataManager, 
                           TrainTracker trainTracker,
                           BackupManager backupManager) {
        this.mtrDataManager = mtrDataManager;
        this.trainTracker = trainTracker;
        this.backupManager = backupManager;
    }
    
    /**
     * 注册事件监听器
     * 由 Fabric/Forge loader 调用
     */
    public void register() {
        // TODO: 注册 MTR 事件监听
        // MTR 可能提供的事件：
        // - 列车到达/离开车站
        // - 线路变化
        // - 车厂调度
        Logger.info("MTR event listener registered");
    }
    
    /**
     * 注销事件监听器
     */
    public void unregister() {
        Logger.info("MTR event listener unregistered");
    }
    
    /**
     * 列车到站事件
     */
    public void onTrainArrive(String trainId, long stationId) {
        Logger.debug("Train " + trainId + " arrived at station " + stationId);
    }
    
    /**
     * 列车离站事件
     */
    public void onTrainDepart(String trainId, long stationId) {
        Logger.debug("Train " + trainId + " departed from station " + stationId);
    }
    
    /**
     * 线路更新事件
     */
    public void onRouteUpdate(long routeId) {
        Logger.debug("Route " + routeId + " updated");
    }
    
    /**
     * 车厂更新事件
     */
    public void onDepotUpdate(long depotId) {
        Logger.debug("Depot " + depotId + " updated");
    }
    
    /**
     * RailwayData 保存事件
     */
    public void onRailwayDataSave() {
        Logger.debug("RailwayData saved");
    }
}
