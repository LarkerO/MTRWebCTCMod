package cn.bg7qvu.mtrwebctc.util;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * 配置属性工具类
 */
public final class ConfigProperties {
    private ConfigProperties() {}
    
    /**
     * 加载属性文件
     * @param file 文件路径
     * @return Properties 对象
     */
    public static Properties load(Path file) {
        Properties props = new Properties();
        
        if (Files.exists(file)) {
            try (InputStream is = Files.newInputStream(file)) {
                props.load(is);
            } catch (IOException e) {
                Logger.error("Failed to load properties: " + e.getMessage());
            }
        }
        
        return props;
    }
    
    /**
     * 保存属性文件
     * @param file 文件路径
     * @param props Properties 对象
     */
    public static void save(Path file, Properties props) {
        try {
            Files.createDirectories(file.getParent());
            try (OutputStream os = Files.newOutputStream(file)) {
                props.store(os, "MTRWebCTC Configuration");
            }
        } catch (IOException e) {
            Logger.error("Failed to save properties: " + e.getMessage());
        }
    }
    
    /**
     * 获取字符串属性
     * @param props Properties 对象
     * @param key 键
     * @param defaultValue 默认值
     * @return 值
     */
    public static String getString(Properties props, String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
    
    /**
     * 获取整数属性
     * @param props Properties 对象
     * @param key 键
     * @param defaultValue 默认值
     * @return 值
     */
    public static int getInt(Properties props, String key, int defaultValue) {
        String value = props.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 获取长整数属性
     * @param props Properties 对象
     * @param key 键
     * @param defaultValue 默认值
     * @return 值
     */
    public static long getLong(Properties props, String key, long defaultValue) {
        String value = props.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 获取布尔属性
     * @param props Properties 对象
     * @param key 键
     * @param defaultValue 默认值
     * @return 值
     */
    public static boolean getBoolean(Properties props, String key, boolean defaultValue) {
        String value = props.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        
        return Boolean.parseBoolean(value);
    }
}
