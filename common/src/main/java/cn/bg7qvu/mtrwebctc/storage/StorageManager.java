package cn.bg7qvu.mtrwebctc.storage;

import cn.bg7qvu.mtrwebctc.util.Logger;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存储管理器
 * 支持 Memory 和 SQLite 两种后端
 */
public class StorageManager {
    private final StorageBackend backend;
    
    public StorageManager(String backendType, String dataDir) {
        switch (backendType.toLowerCase()) {
            case "sqlite":
                this.backend = new SQLiteBackend(dataDir);
                break;
            case "memory":
            default:
                this.backend = new MemoryBackend();
                break;
        }
        Logger.info("Storage backend initialized: " + backendType);
    }
    
    // 统一接口
    public void save(String collection, String key, Map<String, Object> data) {
        backend.save(collection, key, data);
    }
    
    public Map<String, Object> load(String collection, String key) {
        return backend.load(collection, key);
    }
    
    public void delete(String collection, String key) {
        backend.delete(collection, key);
    }
    
    public List<Map<String, Object>> listAll(String collection) {
        return backend.listAll(collection);
    }
    
    public void close() {
        backend.close();
    }
    
    /**
     * 存储后端接口
     */
    private interface StorageBackend {
        void save(String collection, String key, Map<String, Object> data);
        Map<String, Object> load(String collection, String key);
        void delete(String collection, String key);
        List<Map<String, Object>> listAll(String collection);
        void close();
    }
    
    /**
     * 内存存储后端
     */
    private static class MemoryBackend implements StorageBackend {
        private final Map<String, Map<String, Map<String, Object>>> storage = new ConcurrentHashMap<>();
        
        @Override
        public void save(String collection, String key, Map<String, Object> data) {
            storage.computeIfAbsent(collection, k -> new ConcurrentHashMap<>())
                   .put(key, new HashMap<>(data));
        }
        
        @Override
        public Map<String, Object> load(String collection, String key) {
            Map<String, Map<String, Object>> col = storage.get(collection);
            return col != null ? col.get(key) : null;
        }
        
        @Override
        public void delete(String collection, String key) {
            Map<String, Map<String, Object>> col = storage.get(collection);
            if (col != null) {
                col.remove(key);
            }
        }
        
        @Override
        public List<Map<String, Object>> listAll(String collection) {
            Map<String, Map<String, Object>> col = storage.get(collection);
            return col != null ? new ArrayList<>(col.values()) : Collections.emptyList();
        }
        
        @Override
        public void close() {
            storage.clear();
        }
    }
    
    /**
     * SQLite 存储后端
     */
    private static class SQLiteBackend implements StorageBackend {
        private final Connection connection;
        
        public SQLiteBackend(String dataDir) {
            try {
                String dbPath = dataDir + "/mtrwebctc.db";
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                initializeTables();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to initialize SQLite: " + e.getMessage(), e);
            }
        }
        
        private void initializeTables() throws SQLException {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS storage (" +
                    "collection TEXT NOT NULL, " +
                    "key TEXT NOT NULL, " +
                    "data TEXT NOT NULL, " +
                    "updated_at INTEGER NOT NULL, " +
                    "PRIMARY KEY (collection, key)" +
                    ")"
                );
            }
        }
        
        @Override
        public void save(String collection, String key, Map<String, Object> data) {
            try {
                String json = toJson(data);
                String sql = "INSERT OR REPLACE INTO storage (collection, key, data, updated_at) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, collection);
                    stmt.setString(2, key);
                    stmt.setString(3, json);
                    stmt.setLong(4, System.currentTimeMillis());
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                Logger.error("SQLite save error: " + e.getMessage());
            }
        }
        
        @Override
        public Map<String, Object> load(String collection, String key) {
            try {
                String sql = "SELECT data FROM storage WHERE collection = ? AND key = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, collection);
                    stmt.setString(2, key);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return fromJson(rs.getString("data"));
                        }
                    }
                }
            } catch (SQLException e) {
                Logger.error("SQLite load error: " + e.getMessage());
            }
            return null;
        }
        
        @Override
        public void delete(String collection, String key) {
            try {
                String sql = "DELETE FROM storage WHERE collection = ? AND key = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, collection);
                    stmt.setString(2, key);
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                Logger.error("SQLite delete error: " + e.getMessage());
            }
        }
        
        @Override
        public List<Map<String, Object>> listAll(String collection) {
            List<Map<String, Object>> result = new ArrayList<>();
            try {
                String sql = "SELECT data FROM storage WHERE collection = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, collection);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            result.add(fromJson(rs.getString("data")));
                        }
                    }
                }
            } catch (SQLException e) {
                Logger.error("SQLite listAll error: " + e.getMessage());
            }
            return result;
        }
        
        @Override
        public void close() {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                Logger.error("SQLite close error: " + e.getMessage());
            }
        }
        
        private String toJson(Map<String, Object> data) {
            // 简单 JSON 序列化
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, Object> e : data.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(e.getKey()).append("\":");
                Object v = e.getValue();
                if (v instanceof String) {
                    sb.append("\"").append(v).append("\"");
                } else if (v instanceof Number || v instanceof Boolean) {
                    sb.append(v);
                } else {
                    sb.append("\"").append(v).append("\"");
                }
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }
        
        @SuppressWarnings("unchecked")
        private Map<String, Object> fromJson(String json) {
            // 简单 JSON 反序列化（生产环境应使用 GSON）
            Map<String, Object> result = new HashMap<>();
            // TODO: 使用 GSON 解析
            return result;
        }
    }
}
