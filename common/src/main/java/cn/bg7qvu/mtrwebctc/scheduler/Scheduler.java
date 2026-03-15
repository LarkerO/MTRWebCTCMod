package cn.bg7qvu.mtrwebctc.scheduler;

import cn.bg7qvu.mtrwebctc.util.Logger;

import java.util.*;
import java.util.concurrent.*;

/**
 * 任务调度器
 */
public class Scheduler {
    private final ScheduledExecutorService executor;
    private final Map<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();
    
    public Scheduler() {
        this.executor = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "MTRWebCTC-Scheduler");
            t.setDaemon(true);
            return t;
        });
    }
    
    /**
     * 调度周期任务
     */
    public void schedulePeriodic(String name, Runnable task, long initialDelay, long period, TimeUnit unit) {
        if (tasks.containsKey(name)) {
            Logger.warn("Task already exists: " + name);
            return;
        }
        
        ScheduledFuture<?> future = executor.scheduleAtFixedRate(
            () -> {
                try {
                    task.run();
                } catch (Exception e) {
                    Logger.error("Task error [" + name + "]: " + e.getMessage());
                }
            },
            initialDelay, period, unit
        );
        
        tasks.put(name, future);
        Logger.info("Scheduled task: " + name);
    }
    
    /**
     * 调度延迟任务
     */
    public void scheduleDelayed(String name, Runnable task, long delay, TimeUnit unit) {
        ScheduledFuture<?> future = executor.schedule(
            () -> {
                try {
                    task.run();
                    tasks.remove(name);
                } catch (Exception e) {
                    Logger.error("Delayed task error [" + name + "]: " + e.getMessage());
                }
            },
            delay, unit
        );
        
        tasks.put(name, future);
    }
    
    /**
     * 取消任务
     */
    public boolean cancel(String name) {
        ScheduledFuture<?> future = tasks.remove(name);
        if (future != null) {
            future.cancel(false);
            Logger.info("Cancelled task: " + name);
            return true;
        }
        return false;
    }
    
    /**
     * 检查任务是否存在
     */
    public boolean hasTask(String name) {
        ScheduledFuture<?> future = tasks.get(name);
        return future != null && !future.isDone();
    }
    
    /**
     * 获取所有任务名称
     */
    public Set<String> getTaskNames() {
        return new HashSet<>(tasks.keySet());
    }
    
    /**
     * 关闭调度器
     */
    public void shutdown() {
        Logger.info("Shutting down scheduler...");
        tasks.values().forEach(f -> f.cancel(false));
        tasks.clear();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        Logger.info("Scheduler shutdown complete");
    }
}
