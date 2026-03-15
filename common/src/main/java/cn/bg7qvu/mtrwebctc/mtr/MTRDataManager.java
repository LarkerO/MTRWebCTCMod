package cn.bg7qvu.mtrwebctc.mtr;

import cn.bg7qvu.mtrwebctc.backup.BackupManager;
import cn.bg7qvu.mtrwebctc.config.Config;
import cn.bg7qvu.mtrwebctc.model.DepotDTO;
import cn.bg7qvu.mtrwebctc.model.RouteDTO;
import cn.bg7qvu.mtrwebctc.model.StationDTO;
import cn.bg7qvu.mtrwebctc.model.TrainDTO;
import cn.bg7qvu.mtrwebctc.util.Logger;
import mtr.data.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MTR 数据管理器
 * 负责 MTR 数据的读取和修改
 */
public class MTRDataManager {
    private final Config config;
    private final BackupManager backupManager;
    private RailwayData railwayData;
    
    public MTRDataManager(Config config, BackupManager backupManager) {
        this.config = config;
        this.backupManager = backupManager;
    }
    
    /**
     * 设置 RailwayData 引用
     */
    public void setRailwayData(RailwayData railwayData) {
        this.railwayData = railwayData;
        Logger.info("RailwayData reference set");
    }
    
    /**
     * 获取 RailwayData
     */
    public RailwayData getRailwayData() {
        return railwayData;
    }
    
    // ==================== 车站相关 ====================
    
    public List<StationDTO> getAllStations() {
        List<StationDTO> result = new ArrayList<>();
        if (railwayData == null) return result;
        
        for (Station station : railwayData.stations.values()) {
            result.add(convertStation(station));
        }
        return result;
    }
    
    public StationDTO getStation(long id) {
        if (railwayData == null) return null;
        
        Station station = (Station) railwayData.getDataById(transportMode -> railwayData.stations, id);
        return station != null ? convertStation(station) : null;
    }
    
    public boolean updateStation(StationDTO dto) {
        if (railwayData == null) return false;
        
        try {
            Station station = (Station) railwayData.getDataById(
                transportMode -> railwayData.stations, dto.getId());
            if (station == null) return false;
            
            // 更新字段
            station.name = dto.getName();
            station.color = dto.getColor();
            station.zone = dto.getZone();
            
            // 保存
            railwayData.save();
            return true;
        } catch (Exception e) {
            Logger.error("Failed to update station: " + e.getMessage());
            return false;
        }
    }
    
    public List<PlatformDTO> getPlatformsByStation(long stationId) {
        List<PlatformDTO> result = new ArrayList<>();
        if (railwayData == null) return result;
        
        for (Platform platform : railwayData.platforms.values()) {
            if (platform.getStationId() == stationId) {
                result.add(convertPlatform(platform));
            }
        }
        return result;
    }
    
    // ==================== 线路相关 ====================
    
    public List<RouteDTO> getAllRoutes() {
        List<RouteDTO> result = new ArrayList<>();
        if (railwayData == null) return result;
        
        for (Route route : railwayData.routes.values()) {
            result.add(convertRoute(route));
        }
        return result;
    }
    
    public RouteDTO getRoute(long id) {
        if (railwayData == null) return null;
        
        Route route = (Route) railwayData.getDataById(
            transportMode -> railwayData.routes, id);
        return route != null ? convertRoute(route) : null;
    }
    
    public boolean updateRoute(RouteDTO dto) {
        if (railwayData == null) return false;
        
        try {
            Route route = (Route) railwayData.getDataById(
                transportMode -> railwayData.routes, dto.getId());
            if (route == null) return false;
            
            route.name = dto.getName();
            route.color = dto.getColor();
            
            railwayData.save();
            return true;
        } catch (Exception e) {
            Logger.error("Failed to update route: " + e.getMessage());
            return false;
        }
    }
    
    public List<TrainDTO> getTrainsByRoute(long routeId) {
        // TODO: 实现
        return new ArrayList<>();
    }
    
    // ==================== 车厂相关 ====================
    
    public List<DepotDTO> getAllDepots() {
        List<DepotDTO> result = new ArrayList<>();
        if (railwayData == null) return result;
        
        for (Depot depot : railwayData.depots.values()) {
            result.add(convertDepot(depot));
        }
        return result;
    }
    
    public DepotDTO getDepot(long id) {
        if (railwayData == null) return null;
        
        Depot depot = (Depot) railwayData.getDataById(
            transportMode -> railwayData.depots, id);
        return depot != null ? convertDepot(depot) : null;
    }
    
    public boolean updateDepot(DepotDTO dto) {
        if (railwayData == null) return false;
        
        try {
            Depot depot = (Depot) railwayData.getDataById(
                transportMode -> railwayData.depots, dto.getId());
            if (depot == null) return false;
            
            depot.name = dto.getName();
            depot.color = dto.getColor();
            depot.cruisingAltitude = dto.getCruisingAltitude();
            
            railwayData.save();
            return true;
        } catch (Exception e) {
            Logger.error("Failed to update depot: " + e.getMessage());
            return false;
        }
    }
    
    public boolean updateDepotSchedule(long depotId, List<Integer> departures, 
                                        List<Integer> frequencies, boolean useRealTime, 
                                        boolean repeatInfinitely) {
        if (railwayData == null) return false;
        
        try {
            Depot depot = (Depot) railwayData.getDataById(
                transportMode -> railwayData.depots, depotId);
            if (depot == null) return false;
            
            depot.useRealTime = useRealTime;
            depot.repeatInfinitely = repeatInfinitely;
            
            if (departures != null) {
                depot.departures.clear();
                depot.departures.addAll(departures);
            }
            
            if (frequencies != null) {
                depot.frequencies.clear();
                depot.frequencies.addAll(frequencies);
            }
            
            railwayData.save();
            Logger.info("Depot " + depotId + " schedule updated");
            return true;
        } catch (Exception e) {
            Logger.error("Failed to update depot schedule: " + e.getMessage());
            return false;
        }
    }
    
    public List<TrainDTO> getTrainsByDepot(long depotId) {
        // TODO: 实现
        return new ArrayList<>();
    }
    
    // ==================== 列车相关 ====================
    
    public List<TrainDTO> getAllTrains() {
        // TODO: 实现
        return new ArrayList<>();
    }
    
    public TrainDTO getTrain(String trainId) {
        // TODO: 实现
        return null;
    }
    
    // ==================== 转换方法 ====================
    
    private StationDTO convertStation(Station station) {
        StationDTO dto = new StationDTO();
        dto.setId(station.id);
        dto.setName(station.name);
        dto.setColor(station.color);
        dto.setTransportMode(station.transportMode.toString());
        dto.setZone(station.zone);
        dto.setXMin(station.corner1.getX());
        dto.setZMin(station.corner1.getZ());
        dto.setXMax(station.corner2.getX());
        dto.setZMax(station.corner2.getZ());
        return dto;
    }
    
    private PlatformDTO convertPlatform(Platform platform) {
        PlatformDTO dto = new PlatformDTO();
        dto.setId(platform.id);
        dto.setName(platform.name);
        dto.setColor(platform.color);
        dto.setDwellTime(platform.getDwellTime());
        dto.setStationId(platform.getStationId());
        return dto;
    }
    
    private RouteDTO convertRoute(Route route) {
        RouteDTO dto = new RouteDTO();
        dto.setId(route.id);
        dto.setName(route.name);
        dto.setColor(route.color);
        dto.setTransportMode(route.transportMode.toString());
        dto.setRouteType(route.routeType.toString());
        dto.setCircularState(route.circularState.toString());
        dto.setLightRailRoute(route.isLightRailRoute);
        dto.setRouteHidden(route.isHiddenRoute);
        return dto;
    }
    
    private DepotDTO convertDepot(Depot depot) {
        DepotDTO dto = new DepotDTO();
        dto.setId(depot.id);
        dto.setName(depot.name);
        dto.setColor(depot.color);
        dto.setTransportMode(depot.transportMode.toString());
        dto.setUseRealTime(depot.useRealTime);
        dto.setRepeatInfinitely(depot.repeatInfinitely);
        dto.setCruisingAltitude(depot.cruisingAltitude);
        dto.setLastDeployed(depot.lastDeployedMillis);
        return dto;
    }
    
    // PlatformDTO 内部类
    public static class PlatformDTO {
        private long id;
        private String name;
        private int color;
        private int dwellTime;
        private long stationId;
        
        public long getId() { return id; }
        public void setId(long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getColor() { return color; }
        public void setColor(int color) { this.color = color; }
        public int getDwellTime() { return dwellTime; }
        public void setDwellTime(int dwellTime) { this.dwellTime = dwellTime; }
        public long getStationId() { return stationId; }
        public void setStationId(long stationId) { this.stationId = stationId; }
    }
}
