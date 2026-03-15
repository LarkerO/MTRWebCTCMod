package cn.bg7qvu.mtrwebctc.model;

import com.google.gson.annotations.SerializedName;

/**
 * 车站数据传输对象
 */
public class StationDTO {
    private long id;
    private String name;
    private int color;
    private String transportMode;
    @SerializedName("zone")
    private int zone;
    private int xMin;
    private int zMin;
    private int xMax;
    private int zMax;
    
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
    
    public int getZone() {
        return zone;
    }
    
    public void setZone(int zone) {
        this.zone = zone;
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
}
