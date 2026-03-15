package cn.bg7qvu.mtrwebctc.backup;

import cn.bg7qvu.mtrwebctc.config.Config;
import cn.bg7qvu.mtrwebctc.util.Logger;
import mtr.data.RailwayData;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 备份管理器
 */
public class BackupManager {
    private final Path backupDir;
    private final Config config;
    private static final DateTimeFormatter TIMESTAMP_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    
    public BackupManager(Path configDir, Config config) {
        this.backupDir = configDir.resolve("backups");
        this.config = config;
        
        try {
            Files.createDirectories(backupDir);
        } catch (IOException e) {
            Logger.error("Failed to create backup directory: " + e.getMessage());
        }
    }
    
    /**
     * 创建备份
     */
    public String createBackup(String reason) {
        try {
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String backupId = timestamp + "_" + reason;
            Path backupFile = backupDir.resolve(backupId + ".json");
            
            // 获取 RailwayData
            RailwayData railwayData = cn.bg7qvu.mtrwebctc.MTRWebCTCMod.getInstance()
                .getMtrDataManager().getRailwayData();
            
            if (railwayData == null) {
                Logger.error("RailwayData is null, cannot create backup");
                return null;
            }
            
            // 保存到临时文件
            Path tempFile = Files.createTempFile("mtrwebctc-backup", ".json");
            
            // 使用 MTR 的序列化方法
            // TODO: 实现实际的序列化
            
            // 移动到备份目录
            Files.move(tempFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
            
            Logger.info("Backup created: " + backupId);
            
            // 清理旧备份
            cleanupOldBackups();
            
            return backupId;
        } catch (Exception e) {
            Logger.error("Failed to create backup: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 恢复备份
     */
    public boolean restoreBackup(String backupId) {
        try {
            Path backupFile = backupDir.resolve(backupId + ".json");
            
            if (!Files.exists(backupFile)) {
                Logger.error("Backup file not found: " + backupId);
                return false;
            }
            
            // TODO: 实现实际的恢复逻辑
            
            Logger.info("Backup restored: " + backupId);
            return true;
        } catch (Exception e) {
            Logger.error("Failed to restore backup: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 删除备份
     */
    public boolean deleteBackup(String backupId) {
        try {
            Path backupFile = backupDir.resolve(backupId + ".json");
            
            if (!Files.exists(backupFile)) {
                return false;
            }
            
            Files.delete(backupFile);
            Logger.info("Backup deleted: " + backupId);
            return true;
        } catch (Exception e) {
            Logger.error("Failed to delete backup: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取备份列表
     */
    public List<BackupInfo> getBackupList() {
        try {
            return Files.list(backupDir)
                .filter(p -> p.toString().endsWith(".json"))
                .map(this::parseBackupFile)
                .filter(Objects::nonNull)
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .collect(Collectors.toList());
        } catch (IOException e) {
            Logger.error("Failed to list backups: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    private BackupInfo parseBackupFile(Path file) {
        try {
            String filename = file.getFileName().toString();
            String id = filename.replace(".json", "");
            
            BackupInfo info = new BackupInfo();
            info.setId(id);
            info.setFilename(filename);
            info.setSize(Files.size(file));
            info.setCreatedAt(Files.getLastModifiedTime(file).toMillis());
            
            // 解析时间戳和原因
            int underscoreIndex = id.indexOf('_');
            if (underscoreIndex > 0) {
                info.setTimestamp(id.substring(0, underscoreIndex));
                info.setReason(id.substring(underscoreIndex + 1));
            } else {
                info.setTimestamp(id);
                info.setReason("unknown");
            }
            
            return info;
        } catch (IOException e) {
            return null;
        }
    }
    
    private void cleanupOldBackups() {
        int maxBackups = config.getBackup().getMaxBackups();
        
        try {
            List<Path> backups = Files.list(backupDir)
                .filter(p -> p.toString().endsWith(".json"))
                .sorted(Comparator.comparingLong(p -> {
                    try {
                        return -Files.getLastModifiedTime(p).toMillis();
                    } catch (IOException e) {
                        return 0;
                    }
                }))
                .collect(Collectors.toList());
            
            // 删除超出数量的备份
            for (int i = maxBackups; i < backups.size(); i++) {
                Files.delete(backups.get(i));
                Logger.info("Deleted old backup: " + backups.get(i).getFileName());
            }
        } catch (IOException e) {
            Logger.error("Failed to cleanup old backups: " + e.getMessage());
        }
    }
    
    /**
     * 备份信息
     */
    public static class BackupInfo {
        private String id;
        private String filename;
        private String timestamp;
        private String reason;
        private long size;
        private long createdAt;
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }
        public long getCreatedAt() { return createdAt; }
        public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    }
}
