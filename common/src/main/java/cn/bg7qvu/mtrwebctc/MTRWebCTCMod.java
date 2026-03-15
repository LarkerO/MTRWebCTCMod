package cn.bg7qvu.mtrwebctc;

import cn.bg7qvu.mtrwebctc.config.Config;
import cn.bg7qvu.mtrwebctc.config.ConfigLoader;
import cn.bg7qvu.mtrwebctc.server.WebServer;
import cn.bg7qvu.mtrwebctc.mtr.MTRDataManager;
import cn.bg7qvu.mtrwebctc.mtr.TrainTracker;
import cn.bg7qvu.mtrwebctc.backup.BackupManager;
import cn.bg7qvu.mtrwebctc.util.Logger;
import dev.architectury.event.events.common.LifecycleEvent;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;

/**
 * MTRWebCTC Mod 主入口
 * 为 MTR 3.x 提供 Web 控制面板和 API
 */
public class MTRWebCTCMod {
    public static final String MOD_ID = "mtrwebctc";
    public static final String VERSION = "1.0.0";
    
    private static MTRWebCTCMod instance;
    private static MinecraftServer minecraftServer;
    
    private Config config;
    private WebServer webServer;
    private MTRDataManager mtrDataManager;
    private TrainTracker trainTracker;
    private BackupManager backupManager;
    private Path configDir;
    
    public static MTRWebCTCMod getInstance() {
        return instance;
    }
    
    public static MinecraftServer getMinecraftServer() {
        return minecraftServer;
    }
    
    public void initialize() {
        instance = this;
        
        Logger.info("Initializing MTRWebCTC v" + VERSION);
        
        // 注册服务器生命周期事件
        LifecycleEvent.SERVER_STARTING.register(this::onServerStarting);
        LifecycleEvent.SERVER_STARTED.register(this::onServerStarted);
        LifecycleEvent.SERVER_STOPPING.register(this::onServerStopping);
        
        Logger.info("MTRWebCTC initialized successfully");
    }
    
    private void onServerStarting(MinecraftServer server) {
        minecraftServer = server;
        configDir = server.getServerDirectory().toPath().resolve("config").resolve(MOD_ID);
        
        Logger.info("Server starting, loading configuration...");
        
        try {
            // 加载配置
            config = ConfigLoader.load(configDir);
            
            // 初始化备份管理器
            backupManager = new BackupManager(configDir, config);
            
            // 初始化 MTR 数据管理器
            mtrDataManager = new MTRDataManager(config, backupManager);
            
            // 初始化列车追踪器
            trainTracker = new TrainTracker(config, mtrDataManager);
            
            Logger.info("Configuration loaded successfully");
        } catch (Exception e) {
            Logger.error("Failed to load configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void onServerStarted(MinecraftServer server) {
        Logger.info("Server started, initializing web server...");
        
        try {
            // 启动 Web 服务器
            webServer = new WebServer(config, mtrDataManager, trainTracker, backupManager);
            webServer.start();
            
            // 启动列车追踪器
            trainTracker.start();
            
            Logger.info("Web server started on port " + config.getServer().getPort());
        } catch (Exception e) {
            Logger.error("Failed to start web server: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void onServerStopping(MinecraftServer server) {
        Logger.info("Server stopping, shutting down web server...");
        
        try {
            if (trainTracker != null) {
                trainTracker.stop();
            }
            
            if (webServer != null) {
                webServer.stop();
            }
            
            // 创建退出备份
            if (backupManager != null && config.getBackup().isEnabled()) {
                backupManager.createBackup("server-stop");
            }
            
            Logger.info("Web server stopped successfully");
        } catch (Exception e) {
            Logger.error("Error during shutdown: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public Config getConfig() {
        return config;
    }
    
    public WebServer getWebServer() {
        return webServer;
    }
    
    public MTRDataManager getMtrDataManager() {
        return mtrDataManager;
    }
    
    public TrainTracker getTrainTracker() {
        return trainTracker;
    }
    
    public BackupManager getBackupManager() {
        return backupManager;
    }
    
    public Path getConfigDir() {
        return configDir;
    }
    
    public void setConfig(Config config) {
        this.config = config;
    }
}
