package cn.bg7qvu.mtrwebctc.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 命名线程工厂
 * 用于创建有名称的线程
 */
public class NamedThreadFactory implements ThreadFactory {
    private final AtomicInteger counter = new AtomicInteger(0);
    private final String namePrefix;
    private final boolean daemon;
    
    public NamedThreadFactory(String namePrefix) {
        this(namePrefix, false);
    }
    
    public NamedThreadFactory(String namePrefix, boolean daemon) {
        this.namePrefix = namePrefix;
        this.daemon = daemon;
    }
    
    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, namePrefix + "-" + counter.incrementAndGet());
        thread.setDaemon(daemon);
        
        // 设置未捕获异常处理器
        thread.setUncaughtExceptionHandler((t, e) -> {
            Logger.error("Uncaught exception in thread " + t.getName() + ": " + e.getMessage());
        });
        
        return thread;
    }
}
