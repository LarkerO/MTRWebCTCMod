package cn.bg7qvu.mtrwebctc.mtr;

import cn.bg7qvu.mtrwebctc.backup.BackupManager;
import cn.bg7qvu.mtrwebctc.config.Config;
import cn.bg7qvu.mtrwebctc.model.DepotDTO;
import cn.bg7qvu.mtrwebctc.model.PlatformDTO;
import cn.bg7qvu.mtrwebctc.model.RouteDTO;
import cn.bg7qvu.mtrwebctc.model.StationDTO;
import cn.bg7qvu.mtrwebctc.model.TrainDTO;
import cn.bg7qvu.mtrwebctc.util.Logger;
import mtr.data.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * MTR Data Manager
 * Reads and modifies MTR data via the RailwayData API (Set-based collections).
 */
public class MTRDataManager {
    private final Config config;
    private final BackupManager backupManager;
    private RailwayData railwayData;

    public MTRDataManager(Config config, BackupManager backupManager) {
        this.config = config;
        this.backupManager = backupManager;
    }

    public void setRailwayData(RailwayData railwayData) {
        this.railwayData = railwayData;
        Logger.info("RailwayData reference set");
    }

    public RailwayData getRailwayData() {
        return railwayData;
    }

    // ==================== Stations ====================

    public List<StationDTO> getAllStations() {
        List<StationDTO> result = new ArrayList<>();
        if (railwayData == null) return result;

        for (Station station : railwayData.stations) {
            result.add(convertStation(station));
        }
        return result;
    }

    public StationDTO getStation(long id) {
        if (railwayData == null) return null;

        for (Station station : railwayData.stations) {
            if (station.id == id) {
                return convertStation(station);
            }
        }
        return null;
    }

    public boolean updateStation(StationDTO dto) {
        if (railwayData == null) return false;

        try {
            Station station = findById(railwayData.stations, dto.getId());
            if (station == null) return false;

            station.name = dto.getName();
            station.color = dto.getColor();
            station.zone = dto.getZone();

            return true;
        } catch (Exception e) {
            Logger.error("Failed to update station: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get platforms by station. Note: Platform has no getStationId() in MTR 3.x,
     * so this returns all platforms unfiltered. Station association is not available.
     */
    public List<PlatformDTO> getPlatformsByStation(long stationId) {
        List<PlatformDTO> result = new ArrayList<>();
        if (railwayData == null) return result;

        for (Platform platform : railwayData.platforms) {
            result.add(convertPlatform(platform));
        }
        return result;
    }

    // ==================== Routes ====================

    public List<RouteDTO> getAllRoutes() {
        List<RouteDTO> result = new ArrayList<>();
        if (railwayData == null) return result;

        for (Route route : railwayData.routes) {
            result.add(convertRoute(route));
        }
        return result;
    }

    public RouteDTO getRoute(long id) {
        if (railwayData == null) return null;

        for (Route route : railwayData.routes) {
            if (route.id == id) {
                return convertRoute(route);
            }
        }
        return null;
    }

    public boolean updateRoute(RouteDTO dto) {
        if (railwayData == null) return false;

        try {
            Route route = findById(railwayData.routes, dto.getId());
            if (route == null) return false;

            route.name = dto.getName();
            route.color = dto.getColor();

            return true;
        } catch (Exception e) {
            Logger.error("Failed to update route: " + e.getMessage());
            return false;
        }
    }

    public List<TrainDTO> getTrainsByRoute(long routeId) {
        // TODO: implement
        return new ArrayList<>();
    }

    // ==================== Depots ====================

    public List<DepotDTO> getAllDepots() {
        List<DepotDTO> result = new ArrayList<>();
        if (railwayData == null) return result;

        for (Depot depot : railwayData.depots) {
            result.add(convertDepot(depot));
        }
        return result;
    }

    public DepotDTO getDepot(long id) {
        if (railwayData == null) return null;

        for (Depot depot : railwayData.depots) {
            if (depot.id == id) {
                return convertDepot(depot);
            }
        }
        return null;
    }

    public boolean updateDepot(DepotDTO dto) {
        if (railwayData == null) return false;

        try {
            Depot depot = findById(railwayData.depots, dto.getId());
            if (depot == null) return false;

            depot.name = dto.getName();
            depot.color = dto.getColor();
            depot.cruisingAltitude = dto.getCruisingAltitude();

            return true;
        } catch (Exception e) {
            Logger.error("Failed to update depot: " + e.getMessage());
            return false;
        }
    }

    public boolean updateDepotSchedule(long depotId, List<Long> departures,
                                        List<Integer> frequencies, boolean useRealTime,
                                        boolean repeatInfinitely) {
        if (railwayData == null) return false;

        try {
            Depot depot = findById(railwayData.depots, depotId);
            if (depot == null) return false;

            depot.useRealTime = useRealTime;
            depot.repeatInfinitely = repeatInfinitely;

            if (departures != null) {
                depot.departures.clear();
                depot.departures.addAll(departures);
            }

            // frequencies is int[] with private access; update via reflection
            if (frequencies != null) {
                try {
                    Field freqField = Depot.class.getDeclaredField("frequencies");
                    freqField.setAccessible(true);
                    int[] freqArray = new int[frequencies.size()];
                    for (int i = 0; i < frequencies.size(); i++) {
                        freqArray[i] = frequencies.get(i);
                    }
                    freqField.set(depot, freqArray);
                } catch (Exception ex) {
                    Logger.error("Failed to update depot frequencies via reflection: " + ex.getMessage());
                }
            }

            Logger.info("Depot " + depotId + " schedule updated");
            return true;
        } catch (Exception e) {
            Logger.error("Failed to update depot schedule: " + e.getMessage());
            return false;
        }
    }

    public List<TrainDTO> getTrainsByDepot(long depotId) {
        // TODO: implement
        return new ArrayList<>();
    }

    // ==================== Trains ====================

    public List<TrainDTO> getAllTrains() {
        // TODO: implement
        return new ArrayList<>();
    }

    public TrainDTO getTrain(String trainId) {
        // TODO: implement
        return null;
    }

    // ==================== Conversion helpers ====================

    private StationDTO convertStation(Station station) {
        StationDTO dto = new StationDTO();
        dto.setId(station.id);
        dto.setName(station.name);
        dto.setColor(station.color);
        dto.setTransportMode(station.transportMode.toString());
        dto.setZone(station.zone);

        // corner1/corner2 are BlockPos which is not on the common classpath; use reflection
        try {
            Object corner1 = station.corner1;
            Object corner2 = station.corner2;
            if (corner1 != null && corner2 != null) {
                Method getX = corner1.getClass().getMethod("getX");
                Method getZ = corner1.getClass().getMethod("getZ");
                dto.setXMin(((Number) getX.invoke(corner1)).intValue());
                dto.setZMin(((Number) getZ.invoke(corner1)).intValue());
                dto.setXMax(((Number) getX.invoke(corner2)).intValue());
                dto.setZMax(((Number) getZ.invoke(corner2)).intValue());
            }
        } catch (Exception e) {
            Logger.error("Failed to read station corners via reflection: " + e.getMessage());
        }

        return dto;
    }

    private PlatformDTO convertPlatform(Platform platform) {
        PlatformDTO dto = new PlatformDTO();
        dto.setId(platform.id);
        dto.setName(platform.name);
        dto.setColor(platform.color);
        dto.setDwellTime(platform.getDwellTime());
        // Platform has no getStationId(); leave stationId as 0
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
        dto.setRouteHidden(route.isHidden);
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

    // ==================== Utility ====================

    @SuppressWarnings("unchecked")
    private <T extends NameColorDataBase> T findById(Set<T> set, long id) {
        for (T item : set) {
            if (item.id == id) {
                return item;
            }
        }
        return null;
    }
}
