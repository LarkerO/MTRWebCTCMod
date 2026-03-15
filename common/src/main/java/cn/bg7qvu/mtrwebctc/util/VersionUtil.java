package cn.bg7qvu.mtrwebctc.util;

/**
 * 版本工具类
 */
public final class VersionUtil {
    private VersionUtil() {}
    
    /**
     * 比较两个版本号
     * @return 负数: v1 < v2; 0: 相等; 正数: v1 > v2
     */
    public static int compare(String v1, String v2) {
        if (v1 == null || v2 == null) {
            throw new IllegalArgumentException("Version strings cannot be null");
        }
        
        String[] parts1 = v1.split("[.\\-]");
        String[] parts2 = v2.split("[.\\-]");
        
        int length = Math.max(parts1.length, parts2.length);
        
        for (int i = 0; i < length; i++) {
            int num1 = i < parts1.length ? parseVersionPart(parts1[i]) : 0;
            int num2 = i < parts2.length ? parseVersionPart(parts2[i]) : 0;
            
            if (num1 != num2) {
                return num1 - num2;
            }
        }
        
        return 0;
    }
    
    /**
     * 检查版本是否在范围内
     */
    public static boolean isInRange(String version, String min, String max) {
        return compare(version, min) >= 0 && compare(version, max) <= 0;
    }
    
    /**
     * 检查版本是否至少为指定版本
     */
    public static boolean isAtLeast(String version, String min) {
        return compare(version, min) >= 0;
    }
    
    /**
     * 解析版本号部分
     */
    private static int parseVersionPart(String part) {
        // 移除非数字前缀（如 "beta1" -> 1）
        String cleaned = part.replaceAll("^[a-zA-Z]+", "");
        
        try {
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException e) {
            // 如果无法解析，按字母顺序比较
            return part.compareTo("0");
        }
    }
    
    /**
     * 解析 Minecraft 版本
     */
    public static int[] parseMCVersion(String version) {
        // 1.16.5 -> [1, 16, 5]
        String[] parts = version.split("\\.");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                result[i] = Integer.parseInt(parts[i].split("-")[0]);
            } catch (NumberFormatException e) {
                result[i] = 0;
            }
        }
        return result;
    }
    
    /**
     * 检查是否为快照版本
     */
    public static boolean isSnapshot(String version) {
        return version.toLowerCase().contains("snapshot") || 
               version.toLowerCase().contains("pre") ||
               version.toLowerCase().contains("rc");
    }
    
    /**
     * 获取主版本号
     */
    public static String getMajorVersion(String version) {
        String[] parts = version.split("\\.");
        if (parts.length >= 2) {
            return parts[0] + "." + parts[1];
        }
        return version;
    }
}
