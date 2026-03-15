package cn.bg7qvu.mtrwebctc.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * 线路数据传输对象
 */
public class RouteDTO {
    private long id;
    private String name;
    private int color;
    private String transportMode;
    private String routeType;
    @SerializedName("platformIds")
    private List<Long> platformIds;
    @SerializedName("customDestinations")
    private List<String> customDestinations;
    @SerializedName("circularState")
    private String circularState;
    @SerializedName("isLightRailRoute")
    private boolean isLightRailRoute;
    @SerializedName("isRouteHidden")
    private boolean isRouteHidden;
    
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
    
    public String getRouteType() {
        return routeType;
    }
    
    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }
    
    public List<Long> getPlatformIds() {
        return platformIds;
    }
    
    public void setPlatformIds(List<Long> platformIds) {
        this.platformIds = platformIds;
    }
    
    public List<String> getCustomDestinations() {
        return customDestinations;
    }
    
    public void setCustomDestinations(List<String> customDestinations) {
        this.customDestinations = customDestinations;
    }
    
    public String getCircularState() {
        return circularState;
    }
    
    public void setCircularState(String circularState) {
        this.circularState = circularState;
    }
    
    public boolean isLightRailRoute() {
        return isLightRailRoute;
    }
    
    public void setLightRailRoute(boolean lightRailRoute) {
        isLightRailRoute = lightRailRoute;
    }
    
    public boolean isRouteHidden() {
        return isRouteHidden;
    }
    
    public void setRouteHidden(boolean routeHidden) {
        isRouteHidden = routeHidden;
    }
}
