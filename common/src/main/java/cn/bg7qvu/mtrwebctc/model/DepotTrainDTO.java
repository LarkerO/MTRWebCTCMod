package cn.bg7qvu.mtrwebctc.model;

/**
 * 车厂列车配置 DTO
 */
public class DepotTrainDTO {
    private long depotId;
    private String depotName;
    private long routeId;
    private String routeName;
    private int trainCount;
    private int cruisingAltitude;
    private int deployIndex;
    private long lastDeployed;
    private String status;
    
    // Getters
    public long getDepotId() { return depotId; }
    public String getDepotName() { return depotName; }
    public long getRouteId() { return routeId; }
    public String getRouteName() { return routeName; }
    public int getTrainCount() { return trainCount; }
    public int getCruisingAltitude() { return cruisingAltitude; }
    public int getDeployIndex() { return deployIndex; }
    public long getLastDeployed() { return lastDeployed; }
    public String getStatus() { return status; }
    
    // Setters
    public void setDepotId(long depotId) { this.depotId = depotId; }
    public void setDepotName(String depotName) { this.depotName = depotName; }
    public void setRouteId(long routeId) { this.routeId = routeId; }
    public void setRouteName(String routeName) { this.routeName = routeName; }
    public void setTrainCount(int trainCount) { this.trainCount = trainCount; }
    public void setCruisingAltitude(int cruisingAltitude) { this.cruisingAltitude = cruisingAltitude; }
    public void setDeployIndex(int deployIndex) { this.deployIndex = deployIndex; }
    public void setLastDeployed(long lastDeployed) { this.lastDeployed = lastDeployed; }
    public void setStatus(String status) { this.status = status; }
    
    /**
     * 状态枚举
     */
    public static class Status {
        public static final String IDLE = "idle";
        public static final String DEPLOYING = "deploying";
        public static final String RUNNING = "running";
        public static final String RETURNING = "returning";
        public static final String MAINTENANCE = "maintenance";
    }
}
