package cn.bg7qvu.mtrwebctc.model;

import java.util.*;

/**
 * 列车运行计划 DTO
 */
public class ScheduleDTO {
    private long id;
    private long routeId;
    private String routeName;
    private List<ScheduleEntry> entries;
    private boolean useRealTime;
    private boolean repeatInfinitely;
    private long lastUpdated;
    
    /**
     * 时刻表条目
     */
    public static class ScheduleEntry {
        private long stationId;
        private String stationName;
        private int arrivalTime; // 秒，从 0:00 开始
        private int departureTime;
        private int dwellTime;
        
        public long getStationId() { return stationId; }
        public void setStationId(long stationId) { this.stationId = stationId; }
        
        public String getStationName() { return stationName; }
        public void setStationName(String stationName) { this.stationName = stationName; }
        
        public int getArrivalTime() { return arrivalTime; }
        public void setArrivalTime(int arrivalTime) { this.arrivalTime = arrivalTime; }
        
        public int getDepartureTime() { return departureTime; }
        public void setDepartureTime(int departureTime) { this.departureTime = departureTime; }
        
        public int getDwellTime() { return dwellTime; }
        public void setDwellTime(int dwellTime) { this.dwellTime = dwellTime; }
        
        /**
         * 格式化时间（如 "08:30"）
         */
        public String formatArrivalTime() {
            return formatTime(arrivalTime);
        }
        
        public String formatDepartureTime() {
            return formatTime(departureTime);
        }
        
        private String formatTime(int seconds) {
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            int secs = seconds % 60;
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        }
    }
    
    // Getters
    public long getId() { return id; }
    public long getRouteId() { return routeId; }
    public String getRouteName() { return routeName; }
    public List<ScheduleEntry> getEntries() { return entries; }
    public boolean isUseRealTime() { return useRealTime; }
    public boolean isRepeatInfinitely() { return repeatInfinitely; }
    public long getLastUpdated() { return lastUpdated; }
    
    // Setters
    public void setId(long id) { this.id = id; }
    public void setRouteId(long routeId) { this.routeId = routeId; }
    public void setRouteName(String routeName) { this.routeName = routeName; }
    public void setEntries(List<ScheduleEntry> entries) { this.entries = entries; }
    public void setUseRealTime(boolean useRealTime) { this.useRealTime = useRealTime; }
    public void setRepeatInfinitely(boolean repeatInfinitely) { this.repeatInfinitely = repeatInfinitely; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }
}
