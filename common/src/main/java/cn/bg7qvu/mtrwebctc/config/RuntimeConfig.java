package cn.bg7qvu.mtrwebctc.config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 运行时配置
 * 用于存储运行时可修改的配置项
 */
public class RuntimeConfig {
    private final Map<String, Object> config = new ConcurrentHashMap<>();
    
    /**
     * 获取配置值
     * @param key 键
     * @param defaultValue 默认值
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        Object value = config.get(key);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return (T) value;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }
    
    /**
     * 设置配置值
     * @param key 键
     * @param value 值
     */
    public void set(String key, Object value) {
        config.put(key, value);
    }
    
    /**
     * 获取字符串配置
     */
    public String getString(String key, String defaultValue) {
        Object value = config.get(key);
        return value != null ? value.toString() : defaultValue;
    }
    
    /**
     * 获取整数配置
     */
    public int getInt(String key, int defaultValue) {
        Object value = config.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }
    
    /**
     * 获取长整数配置
     */
    public long getLong(String key, long defaultValue) {
        Object value = config.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return defaultValue;
    }
    
    /**
     * 获取布尔配置
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = config.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }
    
    /**
     * 获取双精度配置
     */
    public double getDouble(String key, double defaultValue) {
        Object value = config.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }
    
    /**
     * 检查配置是否存在
     */
    public boolean has(String key) {
        return config.containsKey(key);
    }
    
    /**
     * 移除配置
     */
    public void remove(String key) {
        config.remove(key);
    }
    
    /**
     * 清空所有配置
     */
    public void clear() {
        config.clear();
    }
    
    /**
     * 导出所有配置
     */
    public Map<String, Object> export() {
        return new ConcurrentHashMap<>(config);
    }
    
    /**
     * 导入配置
     */
    public void importConfig(Map<String, Object> map) {
        config.putAll(map);
    }
    
    /**
     * 获取配置数量
     */
    public int size() {
        return config.size();
    }
}
