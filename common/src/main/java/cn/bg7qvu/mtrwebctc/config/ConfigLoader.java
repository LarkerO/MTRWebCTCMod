package cn.bg7qvu.mtrwebctc.config;

import cn.bg7qvu.mtrwebctc.util.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 配置加载器
 */
public class ConfigLoader {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    
    private static final String CONFIG_FILE = "config.json";
    
    public static Config load(Path configDir) throws IOException {
        Path configFile = configDir.resolve(CONFIG_FILE);
        
        if (!Files.exists(configFile)) {
            Logger.info("Config file not found, creating default config");
            Config defaultConfig = new Config();
            save(configDir, defaultConfig);
            return defaultConfig;
        }
        
        Logger.info("Loading config from " + configFile);
        try (BufferedReader reader = Files.newBufferedReader(configFile)) {
            Config config = GSON.fromJson(reader, Config.class);
            
            // 验证配置
            validateConfig(config);
            
            Logger.info("Config loaded successfully");
            return config;
        } catch (Exception e) {
            Logger.error("Failed to load config, using defaults: " + e.getMessage());
            Config defaultConfig = new Config();
            save(configDir, defaultConfig);
            return defaultConfig;
        }
    }
    
    public static void save(Path configDir, Config config) throws IOException {
        Files.createDirectories(configDir);
        Path configFile = configDir.resolve(CONFIG_FILE);
        
        Logger.info("Saving config to " + configFile);
        try (BufferedWriter writer = Files.newBufferedWriter(configFile)) {
            GSON.toJson(config, writer);
        }
        
        Logger.info("Config saved successfully");
    }
    
    public static void reload(Path configDir) throws IOException {
        Config newConfig = load(configDir);
        cn.bg7qvu.mtrwebctc.MTRWebCTCMod.getInstance().setConfig(newConfig);
        Logger.info("Config reloaded successfully");
    }
    
    private static void validateConfig(Config config) {
        // 验证端口范围
        if (config.getServer().getPort() < 1 || config.getServer().getPort() > 65535) {
            Logger.warn("Invalid port " + config.getServer().getPort() + ", using default 7044");
            config.getServer().setPort(7044);
        }
        
        // 验证更新间隔
        if (config.getTrainTracker().getPositionUpdateIntervalMs() < 100) {
            Logger.warn("Position update interval too low, setting to minimum 100ms");
            config.getTrainTracker().setPositionUpdateIntervalMs(100);
        }
        
        // 验证历史保留时间
        if (config.getTrainTracker().getHistoryRetentionMinutes() < 1) {
            Logger.warn("History retention too low, setting to minimum 1 minute");
            config.getTrainTracker().setHistoryRetentionMinutes(1);
        }
        
        // 验证备份数量
        if (config.getBackup().getMaxBackups() < 1) {
            Logger.warn("Max backups too low, setting to minimum 1");
            config.getBackup().setMaxBackups(1);
        }
        
        // 验证存储后端
        String backend = config.getStorage().getBackend();
        if (!backend.equals("memory") && !backend.equals("sqlite")) {
            Logger.warn("Invalid storage backend " + backend + ", using memory");
            config.getStorage().setBackend("memory");
        }
    }
}
