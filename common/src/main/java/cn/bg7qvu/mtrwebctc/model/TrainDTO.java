package cn.bg7qvu.mtrwebctc.model;

import com.google.gson.annotations.SerializedName;

/**
 * 列车数据传输对象
 */
public class TrainDTO {
    @SerializedName("trainId")
    private String trainId;
    @SerializedName("trainUuid")
    private String trainUuid;
    private long routeId;
    private String routeName;
    private String transportMode;
    private double x;
    private double y;
    private double z;
    private double speed;
    @SerializedName("currentStationId")
    private Long currentStationId;
    @SerializedName("currentStationName")
    private String currentStationName;
    @SerializedName("nextStationId")
    private Long nextStationId;
    @SerializedName("nextStationName")
    private String nextStationName;
    @SerializedName("delayMillis")
    private Long delayMillis;
    @SerializedName("depotId")
    private Long depotId;
    @SerializedName("trainCars")
    private int trainCars;
    @SerializedName("segmentCategory")
    private int segmentCategory;
    private double progress;
    
    /**
     * 列车位置记录
     */
    public static class Position {
        public final long timestamp;
        public final double x;
        public final double y;
        public final double z;
        
        public Position(double x, double y, double z) {
            this.timestamp = System.currentTimeMillis();
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        public Position(long timestamp, double x, double y, double z) {
            this.timestamp = timestamp;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
    
    // Getters and Setters
    public String getTrainId() {
        return trainId;
    }
    
    public void setTrainId(String trainId) {
        this.trainId = trainId;
    }
    
    public String getTrainUuid() {
        return trainUuid;
    }
    
    public void setTrainUuid(String trainUuid) {
        this.trainUuid = trainUuid;
    }
    
    public long getRouteId() {
        return routeId;
    }
    
    public void setRouteId(long routeId) {
        this.routeId = routeId;
    }
    
    public String getRouteName() {
        return routeName;
    }
    
    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }
    
    public String getTransportMode() {
        return transportMode;
    }
    
    public void setTransportMode(String transportMode) {
        this.transportMode = transportMode;
    }
    
    public double getX() {
        return x;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public double getY() {
        return y;
    }
    
    public void setY(double y) {
        this.y = y;
    }
    
    public double getZ() {
        return z;
    }
    
    public void setZ(double z) {
        this.z = z;
    }
    
    public double getSpeed() {
        return speed;
    }
    
    public void setSpeed(double speed) {
        this.speed = speed;
    }
    
    public Long getCurrentStationId() {
        return currentStationId;
    }
    
    public void setCurrentStationId(Long currentStationId) {
        this.currentStationId = currentStationId;
    }
    
    public String getCurrentStationName() {
        return currentStationName;
    }
    
    public void setCurrentStationName(String currentStationName) {
        this.currentStationName = currentStationName;
    }
    
    public Long getNextStationId() {
        return nextStationId;
    }
    
    public void setNextStationId(Long nextStationId) {
        this.nextStationId = nextStationId;
    }
    
    public String getNextStationName() {
        return nextStationName;
    }
    
    public void setNextStationName(String nextStationName) {
        this.nextStationName = nextStationName;
    }
    
    public Long getDelayMillis() {
        return delayMillis;
    }
    
    public void setDelayMillis(Long delayMillis) {
        this.delayMillis = delayMillis;
    }
    
    public Long getDepotId() {
        return depotId;
    }
    
    public void setDepotId(Long depotId) {
        this.depotId = depotId;
    }
    
    public int getTrainCars() {
        return trainCars;
    }
    
    public void setTrainCars(int trainCars) {
        this.trainCars = trainCars;
    }
    
    public int getSegmentCategory() {
        return segmentCategory;
    }
    
    public void setSegmentCategory(int segmentCategory) {
        this.segmentCategory = segmentCategory;
    }
    
    public double getProgress() {
        return progress;
    }
    
    public void setProgress(double progress) {
        this.progress = progress;
    }
}
