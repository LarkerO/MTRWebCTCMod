package cn.bg7qvu.mtrwebctc.command;

import cn.bg7qvu.mtrwebctc.MTRWebCTCMod;
import cn.bg7qvu.mtrwebctc.auth.AuthManager;
import cn.bg7qvu.mtrwebctc.config.Config;
import cn.bg7qvu.mtrwebctc.server.WebServer;
import cn.bg7qvu.mtrwebctc.util.HashUtil;
import cn.bg7qvu.mtrwebctc.util.Logger;

/**
 * 游戏内命令处理器
 */
public class CommandHandler {
    private final MTRWebCTCMod mod;
    private final Config config;
    private final AuthManager authManager;
    private final WebServer webServer;
    
    public CommandHandler(MTRWebCTCMod mod, Config config, AuthManager authManager, WebServer webServer) {
        this.mod = mod;
        this.config = config;
        this.authManager = authManager;
        this.webServer = webServer;
    }
    
    /**
     * 处理命令
     * @param sender 命令发送者
     * @param args 命令参数
     * @return 命令执行结果
     */
    public boolean handleCommand(Object sender, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "password":
                return handlePassword(sender, args);
            case "token":
                return handleToken(sender, args);
            case "reload":
                return handleReload(sender);
            case "status":
                return handleStatus(sender);
            case "backup":
                return handleBackup(sender, args);
            case "help":
                sendHelp(sender);
                return true;
            default:
                sendMessage(sender, "Unknown command: " + subCommand);
                return false;
        }
    }
    
    private boolean handlePassword(Object sender, String[] args) {
        if (args.length < 2) {
            sendMessage(sender, "Usage: /mtrwebctc password <password>");
            return false;
        }
        
        String password = args[1];
        
        // 验证密码长度
        if (password.length() < 6) {
            sendMessage(sender, "Password must be at least 6 characters");
            return false;
        }
        
        // 设置密码
        String hash = HashUtil.hashPassword(password);
        config.getAuth().setPasswordHash(hash);
        
        // 保存配置
        try {
            mod.saveConfig();
            sendMessage(sender, "Password set successfully");
            Logger.info("Password updated by " + getSenderName(sender));
        } catch (Exception e) {
            sendMessage(sender, "Failed to save password: " + e.getMessage());
            return false;
        }
        
        return true;
    }
    
    private boolean handleToken(Object sender, String[] args) {
        if (args.length < 2) {
            // 生成新 token
            String token = authManager.generateToken();
            sendMessage(sender, "Generated token: " + token);
            sendMessage(sender, "Use this token in Authorization header: Bearer " + token);
            return true;
        }
        
        String action = args[1].toLowerCase();
        
        if ("revoke".equals(action)) {
            authManager.revokeToken();
            sendMessage(sender, "Token revoked");
            return true;
        }
        
        sendMessage(sender, "Usage: /mtrwebctc token [revoke]");
        return false;
    }
    
    private boolean handleReload(Object sender) {
        try {
            mod.reloadConfig();
            sendMessage(sender, "Configuration reloaded");
            Logger.info("Config reloaded by " + getSenderName(sender));
            return true;
        } catch (Exception e) {
            sendMessage(sender, "Failed to reload: " + e.getMessage());
            return false;
        }
    }
    
    private boolean handleStatus(Object sender) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== MTRWebCTC Status ===\n");
        sb.append("Server: ").append(webServer.isRunning() ? "Running" : "Stopped").append("\n");
        sb.append("Port: ").append(config.getServer().getPort()).append("\n");
        sb.append("Auth: ").append(authManager.isPasswordRequired() ? "Enabled" : "Disabled").append("\n");
        sb.append("Backup: ").append(config.getBackup().isEnabled() ? "Enabled" : "Disabled").append("\n");
        
        sendMessage(sender, sb.toString());
        return true;
    }
    
    private boolean handleBackup(Object sender, String[] args) {
        if (args.length < 2) {
            sendMessage(sender, "Usage: /mtrwebctc backup <create|list|restore>");
            return false;
        }
        
        String action = args[1].toLowerCase();
        
        switch (action) {
            case "create":
                mod.createBackup("manual");
                sendMessage(sender, "Backup created");
                return true;
            case "list":
                // TODO: 列出备份
                sendMessage(sender, "Backup list feature coming soon");
                return true;
            case "restore":
                if (args.length < 3) {
                    sendMessage(sender, "Usage: /mtrwebctc backup restore <id>");
                    return false;
                }
                // TODO: 恢复备份
                sendMessage(sender, "Backup restore feature coming soon");
                return true;
            default:
                sendMessage(sender, "Unknown backup action: " + action);
                return false;
        }
    }
    
    private void sendHelp(Object sender) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== MTRWebCTC Commands ===\n");
        sb.append("/mtrwebctc password <password> - Set web auth password\n");
        sb.append("/mtrwebctc token - Generate auth token\n");
        sb.append("/mtrwebctc token revoke - Revoke current token\n");
        sb.append("/mtrwebctc reload - Reload configuration\n");
        sb.append("/mtrwebctc status - Show server status\n");
        sb.append("/mtrwebctc backup create - Create backup\n");
        sb.append("/mtrwebctc help - Show this help\n");
        
        sendMessage(sender, sb.toString());
    }
    
    // 平台相关方法（由 loader 实现）
    private void sendMessage(Object sender, String message) {
        // 由 Fabric/Forge loader 实现
        mod.sendMessageToPlayer(sender, message);
    }
    
    private String getSenderName(Object sender) {
        // 由 Fabric/Forge loader 实现
        return mod.getPlayerName(sender);
    }
}
