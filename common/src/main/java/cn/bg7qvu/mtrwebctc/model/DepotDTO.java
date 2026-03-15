package cn.bg7qvu.mtrwebctc.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * 车厂数据传输对象
 */
public class DepotDTO {
    private long id;
    private String name;
    private int color;
    private String transportMode;
    private int xMin;
    private int zMin;
    private int xMax;
    private int zMax;
    @SerializedName("routeIds")
    private List<Long> routeIds;
    @SerializedName("useRealTime")
    private boolean useRealTime;
    @SerializedName("repeatInfinitely")
    private boolean repeatInfinitely;
    @SerializedName("cruisingAltitude")
    private int cruisingAltitude;
    private List<Integer> frequencies;
    private List<Integer> departures;
    @SerializedName("deployIndex")
    private int deployIndex;
    @SerializedName("lastDeployed")
    private long lastDeployed;
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getColor() {
        return color;
    }
    
    public void setColor(int color) {
        this.color = color;
    }
    
    public String getTransportMode() {
        return transportMode;
    }
    
    public void setTransportMode(String transportMode) {
        this.transportMode = transportMode;
    }
    
    public int getXMin() {
        return xMin;
    }
    
    public void setXMin(int xMin) {
        this.xMin = xMin;
    }
    
    public int getZMin() {
        return zMin;
    }
    
    public void setZMin(int zMin) {
        this.zMin = zMin;
    }
    
    public int getXMax() {
        return xMax;
    }
    
    public void setXMax(int xMax) {
        this.xMax = xMax;
    }
    
    public int getZMax() {
        return zMax;
    }
    
    public void setZMax(int zMax) {
        this.zMax = zMax;
    }
    
    public List<Long> getRouteIds() {
        return routeIds;
    }
    
    public void setRouteIds(List<Long> routeIds) {
        this.routeIds = routeIds;
    }
    
    public boolean isUseRealTime() {
        return useRealTime;
    }
    
    public void setUseRealTime(boolean useRealTime) {
        this.useRealTime = useRealTime;
    }
    
    public boolean isRepeatInfinitely() {
        return repeatInfinitely;
    }
    
    public void setRepeatInfinitely(boolean repeatInfinitely) {
        this.repeatInfinitely = repeatInfinitely;
    }
    
    public int getCruisingAltitude() {
        return cruisingAltitude;
    }
    
    public void setCruisingAltitude(int cruisingAltitude) {
        this.cruisingAltitude = cruisingAltitude;
    }
    
    public List<Integer> getFrequencies() {
        return frequencies;
    }
    
    public void setFrequencies(List<Integer> frequencies) {
        this.frequencies = frequencies;
    }
    
    public List<Integer> getDepartures() {
        return departures;
    }
    
    public void setDepartures(List<Integer> departures) {
        this.departures = departures;
    }
    
    public int getDeployIndex() {
        return deployIndex;
    }
    
    public void setDeployIndex(int deployIndex) {
        this.deployIndex = deployIndex;
    }
    
    public long getLastDeployed() {
        return lastDeployed;
    }
    
    public void setLastDeployed(long lastDeployed) {
        this.lastDeployed = lastDeployed;
    }
}
