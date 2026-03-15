package cn.bg7qvu.mtrwebctc.util;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件工具类
 */
public final class FileUtils {
    private FileUtils() {}
    
    private static final DateTimeFormatter BACKUP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    /**
     * 确保目录存在
     * @param dir 目录路径
     * @return 是否成功
     */
    public static boolean ensureDirectory(Path dir) {
        try {
            Files.createDirectories(dir);
            return true;
        } catch (IOException e) {
            Logger.error("Failed to create directory: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 生成备份文件名
     * @param prefix 前缀
     * @param suffix 后缀（如 .json）
     * @return 文件名
     */
    public static String generateBackupFilename(String prefix, String suffix) {
        return prefix + "_" + LocalDateTime.now().format(BACKUP_FORMAT) + suffix;
    }
    
    /**
     * 列出目录中的文件
     * @param dir 目录路径
     * @param extension 文件扩展名（可为 null）
     * @return 文件列表
     */
    public static List<Path> listFiles(Path dir, String extension) {
        List<Path> files = new ArrayList<>();
        
        if (!Files.exists(dir)) {
            return files;
        }
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {
                    if (extension == null || path.toString().endsWith(extension)) {
                        files.add(path);
                    }
                }
            }
        } catch (IOException e) {
            Logger.error("Failed to list files: " + e.getMessage());
        }
        
        return files;
    }
    
    /**
     * 删除目录及其内容
     * @param dir 目录路径
     * @return 是否成功
     */
    public static boolean deleteDirectory(Path dir) {
        if (!Files.exists(dir)) {
            return true;
        }
        
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            return true;
        } catch (IOException e) {
            Logger.error("Failed to delete directory: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 复制文件
     * @param source 源文件
     * @param target 目标文件
     * @return 是否成功
     */
    public static boolean copyFile(Path source, Path target) {
        try {
            Files.createDirectories(target.getParent());
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            Logger.error("Failed to copy file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 移动文件
     * @param source 源文件
     * @param target 目标文件
     * @return 是否成功
     */
    public static boolean moveFile(Path source, Path target) {
        try {
            Files.createDirectories(target.getParent());
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            Logger.error("Failed to move file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 读取文件内容为字符串
     * @param file 文件路径
     * @return 文件内容
     */
    public static String readString(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException e) {
            Logger.error("Failed to read file: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * 写入字符串到文件
     * @param file 文件路径
     * @param content 内容
     * @return 是否成功
     */
    public static boolean writeString(Path file, String content) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            Logger.error("Failed to write file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取文件大小
     * @param file 文件路径
     * @return 文件大小（字节），失败返回 -1
     */
    public static long getFileSize(Path file) {
        try {
            return Files.size(file);
        } catch (IOException e) {
            return -1;
        }
    }
    
    /**
     * 检查文件是否存在
     * @param file 文件路径
     * @return 是否存在
     */
    public static boolean exists(Path file) {
        return Files.exists(file);
    }
}
