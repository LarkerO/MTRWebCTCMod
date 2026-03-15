package cn.bg7qvu.mtrwebctc.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 简单的日志工具类
 */
public class Logger {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public static void info(String message) {
        log("INFO", message);
    }
    
    public static void warn(String message) {
        log("WARN", message);
    }
    
    public static void error(String message) {
        log("ERROR", message);
    }
    
    public static void debug(String message) {
        log("DEBUG", message);
    }
    
    private static void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(TIME_FORMAT);
        System.out.println("[" + timestamp + "] [MTRWebCTC/" + level + "] " + message);
    }
}
