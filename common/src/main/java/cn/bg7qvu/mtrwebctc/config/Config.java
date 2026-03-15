package cn.bg7qvu.mtrwebctc.config;

import com.google.gson.annotations.SerializedName;

/**
 * MTRWebCTC 配置类
 */
public class Config {
    private ServerConfig server = new ServerConfig();
    private TrainTrackerConfig trainTracker = new TrainTrackerConfig();
    private WebSocketConfig websocket = new WebSocketConfig();
    private BackupConfig backup = new BackupConfig();
    private StorageConfig storage = new StorageConfig();
    private AuthConfig auth = new AuthConfig();
    
    public ServerConfig getServer() {
        return server;
    }
    
    public void setServer(ServerConfig server) {
        this.server = server;
    }
    
    public TrainTrackerConfig getTrainTracker() {
        return trainTracker;
    }
    
    public void setTrainTracker(TrainTrackerConfig trainTracker) {
        this.trainTracker = trainTracker;
    }
    
    public WebSocketConfig getWebsocket() {
        return websocket;
    }
    
    public void setWebsocket(WebSocketConfig websocket) {
        this.websocket = websocket;
    }
    
    public BackupConfig getBackup() {
        return backup;
    }
    
    public void setBackup(BackupConfig backup) {
        this.backup = backup;
    }
    
    public StorageConfig getStorage() {
        return storage;
    }
    
    public void setStorage(StorageConfig storage) {
        this.storage = storage;
    }
    
    public AuthConfig getAuth() {
        return auth;
    }
    
    public void setAuth(AuthConfig auth) {
        this.auth = auth;
    }
    
    public static class ServerConfig {
        private int port = 7044;
        private String bind = "0.0.0.0";
        @SerializedName("staticResourceMode")
        private String staticResourceMode = "embedded"; // "embedded" or "external"
        
        public int getPort() {
            return port;
        }
        
        public void setPort(int port) {
            this.port = port;
        }
        
        public String getBind() {
            return bind;
        }
        
        public void setBind(String bind) {
            this.bind = bind;
        }
        
        public String getStaticResourceMode() {
            return staticResourceMode;
        }
        
        public void setStaticResourceMode(String staticResourceMode) {
            this.staticResourceMode = staticResourceMode;
        }
    }
    
    public static class TrainTrackerConfig {
        @SerializedName("positionUpdateIntervalMs")
        private long positionUpdateIntervalMs = 1000;
        @SerializedName("historyRetentionMinutes")
        private int historyRetentionMinutes = 5;
        
        public long getPositionUpdateIntervalMs() {
            return positionUpdateIntervalMs;
        }
        
        public void setPositionUpdateIntervalMs(long positionUpdateIntervalMs) {
            this.positionUpdateIntervalMs = positionUpdateIntervalMs;
        }
        
        public int getHistoryRetentionMinutes() {
            return historyRetentionMinutes;
        }
        
        public void setHistoryRetentionMinutes(int historyRetentionMinutes) {
            this.historyRetentionMinutes = historyRetentionMinutes;
        }
    }
    
    public static class WebSocketConfig {
        @SerializedName("pushIntervalMs")
        private long pushIntervalMs = 15000;
        
        public long getPushIntervalMs() {
            return pushIntervalMs;
        }
        
        public void setPushIntervalMs(long pushIntervalMs) {
            this.pushIntervalMs = pushIntervalMs;
        }
    }
    
    public static class BackupConfig {
        private boolean enabled = true;
        @SerializedName("maxBackups")
        private int maxBackups = 3;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public int getMaxBackups() {
            return maxBackups;
        }
        
        public void setMaxBackups(int maxBackups) {
            this.maxBackups = maxBackups;
        }
    }
    
    public static class StorageConfig {
        private String backend = "memory"; // "memory" or "sqlite"
        
        public String getBackend() {
            return backend;
        }
        
        public void setBackend(String backend) {
            this.backend = backend;
        }
    }
    
    public static class AuthConfig {
        @SerializedName("passwordHash")
        private String passwordHash = "";
        @SerializedName("webauthnEnabled")
        private boolean webauthnEnabled = true;
        
        public String getPasswordHash() {
            return passwordHash;
        }
        
        public void setPasswordHash(String passwordHash) {
            this.passwordHash = passwordHash;
        }
        
        public boolean isWebauthnEnabled() {
            return webauthnEnabled;
        }
        
        public void setWebauthnEnabled(boolean webauthnEnabled) {
            this.webauthnEnabled = webauthnEnabled;
        }
    }
}
