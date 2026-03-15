package cn.bg7qvu.mtrwebctc.mtr;

import cn.bg7qvu.mtrwebctc.config.Config;
import cn.bg7qvu.mtrwebctc.model.TrainDTO;
import cn.bg7qvu.mtrwebctc.util.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 列车追踪器
 * 负责追踪列车位置和历史轨迹
 */
public class TrainTracker {
    private final Config config;
    private final MTRDataManager mtrDataManager;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    // 列车位置历史：trainId -> 历史位置列表
    private final Map<String, LinkedList<TrainDTO.Position>> trainHistory = new ConcurrentHashMap<>();
    
    // 当前列车状态
    private final Map<String, TrainDTO> currentTrains = new ConcurrentHashMap<>();
    
    // 线路列车映射
    private final Map<Long, Set<String>> routeTrains = new ConcurrentHashMap<>();
    
    // 车厂列车映射
    private final Map<Long, Set<String>> depotTrains = new ConcurrentHashMap<>();
    
    private volatile boolean running = false;
    
    public TrainTracker(Config config, MTRDataManager mtrDataManager) {
        this.config = config;
        this.mtrDataManager = mtrDataManager;
    }
    
    public void start() {
        if (running) return;
        
        running = true;
        long intervalMs = config.getTrainTracker().getPositionUpdateIntervalMs();
        
        // 位置更新任务
        scheduler.scheduleAtFixedRate(
            this::updateTrainPositions,
            1000, // 延迟 1 秒开始
            intervalMs,
            TimeUnit.MILLISECONDS
        );
        
        // 历史清理任务
        scheduler.scheduleAtFixedRate(
            this::cleanupHistory,
            1,
            1,
            TimeUnit.MINUTES
        );
        
        Logger.info("TrainTracker started, update interval: " + intervalMs + "ms");
    }
    
    public void stop() {
        running = false;
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        Logger.info("TrainTracker stopped");
    }
    
    /**
     * 更新列车位置
     * 从 MTR 获取列车数据并更新缓存
     */
    private void updateTrainPositions() {
        if (!running) return;
        
        try {
            // TODO: 从 MTR 的 TrainServer.simulationHolder 获取列车数据
            // 这里需要反射访问 MTR 内部数据结构
            // 暂时使用占位实现
            
            // 清理已消失的列车
            cleanupInactiveTrains();
            
        } catch (Exception e) {
            Logger.error("Error updating train positions: " + e.getMessage());
        }
    }
    
    /**
     * 清理不活跃的列车
     */
    private void cleanupInactiveTrains() {
        long timeout = 60000; // 60 秒无更新视为消失
        long now = System.currentTimeMillis();
        
        currentTrains.entrySet().removeIf(entry -> {
            // 检查最后更新时间
            TrainDTO train = entry.getValue();
            // 如果列车长时间未更新，移除
            return false; // TODO: 实现时间检查
        });
    }
    
    /**
     * 清理过期历史
     */
    private void cleanupHistory() {
        int retentionMinutes = config.getTrainTracker().getHistoryRetentionMinutes();
        long cutoffTime = System.currentTimeMillis() - (retentionMinutes * 60 * 1000L);
        
        trainHistory.forEach((trainId, history) -> {
            // 移除过期的历史记录
            history.removeIf(pos -> pos.timestamp < cutoffTime);
        });
        
        // 移除空历史
        trainHistory.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
    
    // ==================== 公共 API ====================
    
    /**
     * 获取所有列车
     */
    public List<TrainDTO> getAllTrains() {
        return new ArrayList<>(currentTrains.values());
    }
    
    /**
     * 获取单个列车
     */
    public TrainDTO getTrain(String trainId) {
        return currentTrains.get(trainId);
    }
    
    /**
     * 获取线路上的列车
     */
    public List<TrainDTO> getRouteTrains(long routeId) {
        Set<String> trainIds = routeTrains.get(routeId);
        if (trainIds == null || trainIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        return trainIds.stream()
            .map(currentTrains::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取车厂内的列车
     */
    public List<TrainDTO> getDepotTrains(long depotId) {
        Set<String> trainIds = depotTrains.get(depotId);
        if (trainIds == null || trainIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        return trainIds.stream()
            .map(currentTrains::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取列车历史轨迹
     */
    public List<TrainDTO.Position> getTrainHistory(String trainId) {
        LinkedList<TrainDTO.Position> history = trainHistory.get(trainId);
        return history != null ? new ArrayList<>(history) : Collections.emptyList();
    }
    
    /**
     * 获取当前列车数量
     */
    public int getTrainCount() {
        return currentTrains.size();
    }
    
    /**
     * 获取连接数
     */
    public int getHistoryCount() {
        return trainHistory.size();
    }
    
    // ==================== 内部方法 ====================
    
    /**
     * 记录列车位置
     */
    public void recordTrainPosition(String trainId, double x, double y, double z) {
        TrainDTO.Position position = new TrainDTO.Position(x, y, z);
        
        trainHistory.computeIfAbsent(trainId, k -> new LinkedList<>()).addFirst(position);
        
        // 限制历史记录数量
        LinkedList<TrainDTO.Position> history = trainHistory.get(trainId);
        while (history.size() > 1000) {
            history.removeLast();
        }
    }
    
    /**
     * 更新列车状态
     */
    public void updateTrainStatus(TrainDTO train) {
        String trainId = train.getTrainId();
        currentTrains.put(trainId, train);
        
        // 更新线路映射
        long routeId = train.getRouteId();
        routeTrains.computeIfAbsent(routeId, k -> ConcurrentHashMap.newKeySet()).add(trainId);
        
        // 更新车厂映射
        Long depotId = train.getDepotId();
        if (depotId != null) {
            depotTrains.computeIfAbsent(depotId, k -> ConcurrentHashMap.newKeySet()).add(trainId);
        }
        
        // 记录位置
        recordTrainPosition(trainId, train.getX(), train.getY(), train.getZ());
    }
    
    /**
     * 移除列车
     */
    public void removeTrain(String trainId) {
        TrainDTO train = currentTrains.remove(trainId);
        if (train != null) {
            // 从线路映射移除
            long routeId = train.getRouteId();
            Set<String> routeSet = routeTrains.get(routeId);
            if (routeSet != null) {
                routeSet.remove(trainId);
            }
            
            // 从车厂映射移除
            Long depotId = train.getDepotId();
            if (depotId != null) {
                Set<String> depotSet = depotTrains.get(depotId);
                if (depotSet != null) {
                    depotSet.remove(trainId);
                }
            }
        }
        // 保留历史记录，会在清理任务中自动移除
    }
    
    /**
     * 清空所有数据
     */
    public void clear() {
        currentTrains.clear();
        trainHistory.clear();
        routeTrains.clear();
        depotTrains.clear();
    }
}
