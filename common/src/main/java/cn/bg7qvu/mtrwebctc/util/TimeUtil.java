package cn.bg7qvu.mtrwebctc.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 时间工具类
 */
public final class TimeUtil {
    private TimeUtil() {}
    
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private static final DateTimeFormatter READABLE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FILE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    /**
     * 获取当前时间戳（毫秒）
     */
    public static long now() {
        return System.currentTimeMillis();
    }
    
    /**
     * 获取当前时间戳（秒）
     */
    public static long nowSeconds() {
        return System.currentTimeMillis() / 1000;
    }
    
    /**
     * 格式化为 ISO 8601 格式
     */
    public static String formatIso(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("UTC"))
                .format(ISO_FORMATTER);
    }
    
    /**
     * 格式化为可读格式
     */
    public static String formatReadable(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
                .format(READABLE_FORMATTER);
    }
    
    /**
     * 格式化为文件名格式
     */
    public static String formatForFilename(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
                .format(FILE_FORMATTER);
    }
    
    /**
     * 获取当前 ISO 格式时间
     */
    public static String nowIso() {
        return formatIso(now());
    }
    
    /**
     * 获取当前可读格式时间
     */
    public static String nowReadable() {
        return formatReadable(now());
    }
    
    /**
     * 获取当前文件名格式时间
     */
    public static String nowForFilename() {
        return formatForFilename(now());
    }
    
    /**
     * 计算时间差（毫秒）
     */
    public static long diffMs(long start, long end) {
        return end - start;
    }
    
    /**
     * 计算时间差（秒）
     */
    public static long diffSeconds(long start, long end) {
        return (end - start) / 1000;
    }
    
    /**
     * 计算从开始到现在的时间差（毫秒）
     */
    public static long elapsed(long start) {
        return now() - start;
    }
    
    /**
     * 检查时间戳是否过期
     */
    public static boolean isExpired(long timestamp, long ttlMs) {
        return now() > timestamp + ttlMs;
    }
    
    /**
     * 检查时间戳是否在时间窗口内
     */
    public static boolean isInWindow(long timestamp, long windowStart, long windowEnd) {
        long now = now();
        return timestamp >= windowStart && timestamp <= windowEnd;
    }
    
    /**
     * 获取今天的开始时间（00:00:00）
     */
    public static long todayStart() {
        return LocalDateTime.now()
                .withHour(0).withMinute(0).withSecond(0).withNano(0)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }
    
    /**
     * 获取今天的结束时间（23:59:59）
     */
    public static long todayEnd() {
        return LocalDateTime.now()
                .withHour(23).withMinute(59).withSecond(59).withNano(999999999)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }
}
