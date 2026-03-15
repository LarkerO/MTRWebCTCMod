package cn.bg7qvu.mtrwebctc.mtr;

import cn.bg7qvu.mtrwebctc.config.Config;
import cn.bg7qvu.mtrwebctc.model.TrainDTO;
import cn.bg7qvu.mtrwebctc.util.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 列车追踪器
 * 负责追踪列车位置和历史轨迹
 */
public class TrainTracker {
    private final Config config;
    private final MTRDataManager mtrDataManager;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    // 列车位置历史：trainId -> 历史位置列表
    private final Map<String, LinkedList<TrainDTO.Position>> trainHistory = new ConcurrentHashMap<>();
    
    // 当前列车状态
    private final Map<String, TrainDTO> currentTrains = new ConcurrentHashMap<>();
    
    private volatile boolean running = false;
    
    public TrainTracker(Config config, MTRDataManager mtrDataManager) {
        this.config = config;
        this.mtrDataManager = mtrDataManager;
    }
    
    public void start() {
        if (running) return;
        
        running = true;
        long intervalMs = config.getTrainTracker().getPositionUpdateIntervalMs();
        
        scheduler.scheduleAtFixedRate(
            this::updateTrainPositions,
            0,
            intervalMs,
            TimeUnit.MILLISECONDS
        );
        
        // 清理过期历史的任务
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
        }
        Logger.info("TrainTracker stopped");
    }
    
    private void updateTrainPositions() {
        try {
            // TODO: 从 MTR 获取列车数据
            // 这里需要访问 MTR 的内部数据结构
            // 暂时使用占位实现
            
        } catch (Exception e) {
            Logger.error("Error updating train positions: " + e.getMessage());
        }
    }
    
    private void cleanupHistory() {
        int retentionMinutes = config.getTrainTracker().getHistoryRetentionMinutes();
        long cutoffTime = System.currentTimeMillis() - (retentionMinutes * 60 * 1000L);
        
        trainHistory.forEach((trainId, history) -> {
            // 移除过期的历史记录
            history.removeIf(pos -> {
                // 假设每个位置都有时间戳
                return false; // TODO: 实现时间戳检查
            });
        });
    }
    
    /**
     * 获取列车历史轨迹
     */
    public List<TrainDTO.Position> getTrainHistory(String trainId) {
        LinkedList<TrainDTO.Position> history = trainHistory.get(trainId);
        return history != null ? new ArrayList<>(history) : Collections.emptyList();
    }
    
    /**
     * 获取所有当前列车
     */
    public Map<String, TrainDTO> getCurrentTrains() {
        return new HashMap<>(currentTrains);
    }
    
    /**
     * 记录列车位置
     */
    public void recordTrainPosition(String trainId, double x, double y, double z) {
        TrainDTO.Position position = new TrainDTO.Position(x, y, z);
        
        trainHistory.computeIfAbsent(trainId, k -> new LinkedList<>()).addFirst(position);
        
        // 限制历史记录数量
        LinkedList<TrainDTO.Position> history = trainHistory.get(trainId);
        while (history.size() > 1000) { // 最多保留1000个点
            history.removeLast();
        }
    }
    
    /**
     * 更新列车状态
     */
    public void updateTrainStatus(TrainDTO train) {
        currentTrains.put(train.getTrainId(), train);
    }
    
    /**
     * 移除列车
     */
    public void removeTrain(String trainId) {
        currentTrains.remove(trainId);
        // 保留历史记录，但会在清理任务中自动移除
    }
}
