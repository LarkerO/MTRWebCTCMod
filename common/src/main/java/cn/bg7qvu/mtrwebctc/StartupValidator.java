package cn.bg7qvu.mtrwebctc;

import cn.bg7qvu.mtrwebctc.util.Logger;
import cn.bg7qvu.mtrwebctc.util.VersionUtil;

import java.util.*;

/**
 * 初始化检查器
 * 检查运行环境是否满足要求
 */
public class StartupValidator {
    
    /**
     * 验证结果
     */
    public static class ValidationResult {
        private final boolean success;
        private final List<String> errors;
        private final List<String> warnings;
        
        public ValidationResult(boolean success, List<String> errors, List<String> warnings) {
            this.success = success;
            this.errors = errors;
            this.warnings = warnings;
        }
        
        public boolean isSuccess() { return success; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
    }
    
    /**
     * 运行所有检查
     */
    public static ValidationResult validate(String mcVersion, String loaderType, String mtrVersion) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // 检查 Java 版本
        checkJavaVersion(errors, warnings);
        
        // 检查 Minecraft 版本
        checkMCVersion(mcVersion, errors, warnings);
        
        // 检查模组加载器
        checkLoader(loaderType, errors, warnings);
        
        // 检查 MTR
        checkMTR(mtrVersion, errors, warnings);
        
        // 检查端口
        checkPort(errors, warnings);
        
        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }
    
    /**
     * 检查 Java 版本
     */
    private static void checkJavaVersion(List<String> errors, List<String> warnings) {
        String javaVersion = System.getProperty("java.version");
        int majorVersion = parseJavaMajorVersion(javaVersion);
        
        Logger.info("Java version: " + javaVersion + " (major: " + majorVersion + ")");
        
        if (majorVersion < 8) {
            errors.add("Java 8 or higher is required (found Java " + majorVersion + ")");
        } else if (majorVersion < 17) {
            warnings.add("Java 17+ is recommended for Minecraft 1.18+");
        }
    }
    
    /**
     * 检查 Minecraft 版本
     */
    private static void checkMCVersion(String version, List<String> errors, List<String> warnings) {
        Logger.info("Minecraft version: " + version);
        
        boolean supported = false;
        for (String supportedVersion : Constants.SUPPORTED_MC_VERSIONS) {
            if (version.startsWith(supportedVersion.substring(0, 4))) { // 简单匹配主版本
                supported = true;
                break;
            }
        }
        
        if (!supported) {
            warnings.add("Minecraft " + version + " is not officially tested");
        }
    }
    
    /**
     * 检查模组加载器
     */
    private static void checkLoader(String loaderType, List<String> errors, List<String> warnings) {
        Logger.info("Mod loader: " + loaderType);
        
        if (!"fabric".equalsIgnoreCase(loaderType) && !"forge".equalsIgnoreCase(loaderType)) {
            errors.add("Unsupported mod loader: " + loaderType);
        }
    }
    
    /**
     * 检查 MTR
     */
    private static void checkMTR(String mtrVersion, List<String> errors, List<String> warnings) {
        if (mtrVersion == null || mtrVersion.isEmpty()) {
            errors.add("MTR mod not found! This mod requires MTR 3.x to function.");
            return;
        }
        
        Logger.info("MTR version: " + mtrVersion);
        
        // 检查版本范围
        if (!VersionUtil.isInRange(mtrVersion, Constants.MTR_MIN_VERSION, Constants.MTR_MAX_VERSION)) {
            if (VersionUtil.compare(mtrVersion, Constants.MTR_MIN_VERSION) < 0) {
                errors.add("MTR version too old. Requires " + Constants.MTR_MIN_VERSION + "+, found " + mtrVersion);
            } else {
                warnings.add("MTR " + mtrVersion + " may not be fully compatible. Tested with MTR 3.x");
            }
        }
    }
    
    /**
     * 检查端口
     */
    private static void checkPort(List<String> errors, List<String> warnings) {
        int port = Constants.DEFAULT_PORT;
        
        if (port < 1024 || port > 65535) {
            errors.add("Invalid port: " + port);
        } else if (port < 1024) {
            warnings.add("Port " + port + " requires root privileges on Unix systems");
        }
    }
    
    /**
     * 解析 Java 主版本号
     */
    private static int parseJavaMajorVersion(String version) {
        if (version.startsWith("1.")) {
            // Java 8 及之前: 1.8.0_xxx
            return Integer.parseInt(version.substring(2, 3));
        } else {
            // Java 9+: 9, 11, 17, etc.
            int dotIndex = version.indexOf('.');
            if (dotIndex > 0) {
                return Integer.parseInt(version.substring(0, dotIndex));
            }
            return Integer.parseInt(version.split("-")[0]);
        }
    }
}
