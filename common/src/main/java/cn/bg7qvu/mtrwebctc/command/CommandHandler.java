package cn.bg7qvu.mtrwebctc.command;

import cn.bg7qvu.mtrwebctc.MTRWebCTCMod;
import cn.bg7qvu.mtrwebctc.auth.AuthManager;
import cn.bg7qvu.mtrwebctc.backup.BackupManager;
import cn.bg7qvu.mtrwebctc.config.Config;
import cn.bg7qvu.mtrwebctc.config.ConfigLoader;
import cn.bg7qvu.mtrwebctc.util.HashUtil;
import cn.bg7qvu.mtrwebctc.util.Logger;

/**
 * In-game command handler for /mtrwebctc commands.
 * Platform-specific code should call handleCommand() with the appropriate sender.
 */
public class CommandHandler {
    private final MTRWebCTCMod mod;

    public CommandHandler(MTRWebCTCMod mod) {
        this.mod = mod;
    }

    /**
     * Handle a command.
     * @param args command arguments
     * @return result messages
     */
    public String handleCommand(String[] args) {
        if (args.length == 0) {
            return getHelp();
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "password":
                return handlePassword(args);
            case "token":
                return handleToken(args);
            case "reload":
                return handleReload();
            case "status":
                return handleStatus();
            case "backup":
                return handleBackup(args);
            case "help":
                return getHelp();
            default:
                return "Unknown command: " + subCommand;
        }
    }

    private String handlePassword(String[] args) {
        if (args.length < 2) {
            return "Usage: /mtrwebctc password <password>";
        }

        String password = args[1];
        if (password.length() < 6) {
            return "Password must be at least 6 characters";
        }

        Config config = mod.getConfig();
        if (config == null) return "Config not loaded";

        String hash = HashUtil.hashPassword(password);
        config.getAuth().setPasswordHash(hash);

        try {
            ConfigLoader.save(mod.getConfigDir(), config);
            Logger.info("Password updated via command");
            return "Password set successfully";
        } catch (Exception e) {
            return "Failed to save password: " + e.getMessage();
        }
    }

    private String handleToken(String[] args) {
        Config config = mod.getConfig();
        if (config == null) return "Config not loaded";

        AuthManager authManager = new AuthManager(config);
        String token = authManager.generateToken();
        return "Generated token: " + token + "\nUse in Authorization header: Bearer " + token;
    }

    private String handleReload() {
        try {
            ConfigLoader.reload(mod.getConfigDir());
            Logger.info("Config reloaded via command");
            return "Configuration reloaded";
        } catch (Exception e) {
            return "Failed to reload: " + e.getMessage();
        }
    }

    private String handleStatus() {
        Config config = mod.getConfig();
        if (config == null) return "Config not loaded";

        StringBuilder sb = new StringBuilder();
        sb.append("=== MTRWebCTC Status ===\n");
        sb.append("Version: ").append(MTRWebCTCMod.VERSION).append("\n");
        sb.append("Port: ").append(config.getServer().getPort()).append("\n");
        sb.append("Backup: ").append(config.getBackup().isEnabled() ? "Enabled" : "Disabled").append("\n");
        return sb.toString();
    }

    private String handleBackup(String[] args) {
        if (args.length < 2) {
            return "Usage: /mtrwebctc backup <create|list>";
        }

        String action = args[1].toLowerCase();
        BackupManager backupManager = mod.getBackupManager();
        if (backupManager == null) return "Backup manager not initialized";

        switch (action) {
            case "create":
                String id = backupManager.createBackup("manual-command");
                return id != null ? "Backup created: " + id : "Failed to create backup";
            case "list":
                java.util.List<BackupManager.BackupInfo> backups = backupManager.getBackupList();
                if (backups.isEmpty()) return "No backups found";
                StringBuilder sb = new StringBuilder("=== Backups ===\n");
                for (BackupManager.BackupInfo info : backups) {
                    sb.append(info.getId()).append(" (").append(info.getSize()).append(" bytes)\n");
                }
                return sb.toString();
            default:
                return "Unknown backup action: " + action;
        }
    }

    private String getHelp() {
        return "=== MTRWebCTC Commands ===\n" +
            "/mtrwebctc password <password> - Set web auth password\n" +
            "/mtrwebctc token - Generate auth token\n" +
            "/mtrwebctc reload - Reload configuration\n" +
            "/mtrwebctc status - Show server status\n" +
            "/mtrwebctc backup create - Create backup\n" +
            "/mtrwebctc backup list - List backups\n" +
            "/mtrwebctc help - Show this help";
    }
}
