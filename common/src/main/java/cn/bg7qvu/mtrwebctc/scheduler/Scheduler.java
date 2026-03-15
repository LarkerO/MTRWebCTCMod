package cn.bg7qvu.mtrwebctc.scheduler;

import cn.bg7qvu.mtrwebctc.util.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 任务调度器
 * 用于管理定时任务
 */
public class Scheduler {
    private final ScheduledExecutorService executor;
    private final Map<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();
    private volatile boolean running = false;
    
    public Scheduler() {
        this.executor = Executors.newScheduledThreadPool(2);
    }
    
    /**
     * 启动调度器
     */
    public void start() {
        running = true;
        Logger.info("Scheduler started");
    }
    
    /**
     * 停止调度器
     */
    public void stop() {
        running = false;
        
        // 取消所有任务
        tasks.values().forEach(task -> task.cancel(false));
        tasks.clear();
        
        // 关闭执行器
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        Logger.info("Scheduler stopped");
    }
    
    /**
     * 添加周期性任务
     * @param taskId 任务ID
     * @param task 任务
     * @param initialDelay 初始延迟（毫秒）
     * @param period 周期（毫秒）
     */
    public void scheduleAtFixedRate(String taskId, Runnable task, 
                                     long initialDelay, long period) {
        if (tasks.containsKey(taskId)) {
            Logger.warn("Task " + taskId + " already exists, replacing");
            cancel(taskId);
        }
        
        ScheduledFuture<?> future = executor.scheduleAtFixedRate(
            () -> {
                try {
                    task.run();
                } catch (Exception e) {
                    Logger.error("Task " + taskId + " error: " + e.getMessage());
                }
            },
            initialDelay,
            period,
            TimeUnit.MILLISECONDS
        );
        
        tasks.put(taskId, future);
        Logger.info("Scheduled task: " + taskId + " (period: " + period + "ms)");
    }
    
    /**
     * 添加延迟任务
     * @param taskId 任务ID
     * @param task 任务
     * @param delay 延迟（毫秒）
     */
    public void schedule(String taskId, Runnable task, long delay) {
        ScheduledFuture<?> future = executor.schedule(
            () -> {
                try {
                    task.run();
                    tasks.remove(taskId);
                } catch (Exception e) {
                    Logger.error("Task " + taskId + " error: " + e.getMessage());
                }
            },
            delay,
            TimeUnit.MILLISECONDS
        );
        
        tasks.put(taskId, future);
        Logger.info("Scheduled one-time task: " + taskId + " (delay: " + delay + "ms)");
    }
    
    /**
     * 取消任务
     * @param taskId 任务ID
     */
    public void cancel(String taskId) {
        ScheduledFuture<?> future = tasks.remove(taskId);
        if (future != null) {
            future.cancel(false);
            Logger.info("Cancelled task: " + taskId);
        }
    }
    
    /**
     * 检查任务是否存在
     * @param taskId 任务ID
     * @return 是否存在
     */
    public boolean hasTask(String taskId) {
        ScheduledFuture<?> future = tasks.get(taskId);
        return future != null && !future.isDone() && !future.isCancelled();
    }
    
    /**
     * 获取活跃任务数量
     * @return 任务数量
     */
    public int getActiveTaskCount() {
        return (int) tasks.values().stream()
            .filter(f -> !f.isDone() && !f.isCancelled())
            .count();
    }
    
    /**
     * 取消所有任务
     */
    public void cancelAll() {
        tasks.keySet().forEach(this::cancel);
    }
}
