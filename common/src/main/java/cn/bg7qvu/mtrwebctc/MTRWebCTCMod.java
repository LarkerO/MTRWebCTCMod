package cn.bg7qvu.mtrwebctc;

import cn.bg7qvu.mtrwebctc.config.Config;
import cn.bg7qvu.mtrwebctc.config.ConfigLoader;
import cn.bg7qvu.mtrwebctc.server.WebServer;
import cn.bg7qvu.mtrwebctc.mtr.MTRDataManager;
import cn.bg7qvu.mtrwebctc.mtr.TrainTracker;
import cn.bg7qvu.mtrwebctc.backup.BackupManager;
import cn.bg7qvu.mtrwebctc.util.Logger;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;

/**
 * MTRWebCTC Mod main entry point.
 * Provides a Web control panel and API for MTR 3.x.
 *
 * Note: This class does NOT directly reference Minecraft or Architectury classes.
 * The platform-specific entry points (Fabric/Forge) register lifecycle events
 * and delegate to onServerStarting/Started/Stopping.
 */
public class MTRWebCTCMod {
    public static final String MOD_ID = "mtrwebctc";
    public static final String VERSION = "1.0.0";

    private static MTRWebCTCMod instance;
    private static Object minecraftServer;

    private Config config;
    private WebServer webServer;
    private MTRDataManager mtrDataManager;
    private TrainTracker trainTracker;
    private BackupManager backupManager;
    private Path configDir;

    public static MTRWebCTCMod getInstance() {
        return instance;
    }

    public static Object getMinecraftServer() {
        return minecraftServer;
    }

    /**
     * Called from platform-specific entry points (Fabric/Forge ModInitializer).
     * Does NOT register lifecycle events here — that is done by the platform modules.
     */
    public void initialize() {
        instance = this;
        Logger.info("Initializing MTRWebCTC v" + VERSION);
        Logger.info("MTRWebCTC initialized successfully");
    }

    /**
     * Called by platform module when the server is starting.
     * The server object is MinecraftServer but typed as Object to avoid
     * Minecraft class dependency in the common module.
     */
    public void onServerStarting(Object server) {
        minecraftServer = server;

        // Resolve config directory via reflection: server.getServerDirectory()
        try {
            File serverDir = getServerDirectory(server);
            configDir = serverDir.toPath().resolve("config").resolve(MOD_ID);
        } catch (Exception e) {
            Logger.error("Failed to resolve server directory: " + e.getMessage());
            configDir = new File("config/" + MOD_ID).toPath();
        }

        Logger.info("Server starting, loading configuration...");

        try {
            config = ConfigLoader.load(configDir);
            backupManager = new BackupManager(configDir, config);
            mtrDataManager = new MTRDataManager(config, backupManager);
            trainTracker = new TrainTracker(config, mtrDataManager);
            Logger.info("Configuration loaded successfully");
        } catch (Exception e) {
            Logger.error("Failed to load configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Called by platform module when the server has started.
     */
    public void onServerStarted(Object server) {
        Logger.info("Server started, initializing web server...");

        try {
            webServer = new WebServer(config, mtrDataManager, trainTracker, backupManager);
            webServer.start();
            trainTracker.start();
            Logger.info("Web server started on port " + config.getServer().getPort());
        } catch (Exception e) {
            Logger.error("Failed to start web server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Called by platform module when the server is stopping.
     */
    public void onServerStopping(Object server) {
        Logger.info("Server stopping, shutting down web server...");

        try {
            if (trainTracker != null) {
                trainTracker.stop();
            }
            if (webServer != null) {
                webServer.stop();
            }
            if (backupManager != null && config.getBackup().isEnabled()) {
                backupManager.createBackup("server-stop");
            }
            Logger.info("Web server stopped successfully");
        } catch (Exception e) {
            Logger.error("Error during shutdown: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get server directory via reflection to avoid MinecraftServer dependency.
     */
    private File getServerDirectory(Object server) throws Exception {
        // Try Mojang-mapped method name first
        try {
            Method m = server.getClass().getMethod("getServerDirectory");
            Object result = m.invoke(server);
            if (result instanceof File) {
                return (File) result;
            }
        } catch (NoSuchMethodException ignored) {
        }
        // Fallback: try "func_240776_a_" (Forge SRG) or other names
        for (Method m : server.getClass().getMethods()) {
            if (m.getReturnType() == File.class && m.getParameterCount() == 0) {
                Object result = m.invoke(server);
                if (result instanceof File) {
                    return (File) result;
                }
            }
        }
        return new File(".");
    }

    // Getters
    public Config getConfig() { return config; }
    public WebServer getWebServer() { return webServer; }
    public MTRDataManager getMtrDataManager() { return mtrDataManager; }
    public TrainTracker getTrainTracker() { return trainTracker; }
    public BackupManager getBackupManager() { return backupManager; }
    public Path getConfigDir() { return configDir; }
    public void setConfig(Config config) { this.config = config; }
}
