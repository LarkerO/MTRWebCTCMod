package cn.bg7qvu.mtrwebctc.util;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.*;

/**
 * JSON 工具类
 */
public final class JsonUtil {
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .serializeNulls()
        .create();
    
    private static final Gson GSON_COMPACT = new GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create();
    
    private JsonUtil() {}
    
    /**
     * 序列化为 JSON
     */
    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }
    
    /**
     * 序列化为紧凑 JSON
     */
    public static String toJsonCompact(Object obj) {
        return GSON_COMPACT.toJson(obj);
    }
    
    /**
     * 从 JSON 反序列化
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }
    
    public static <T> T fromJson(String json, Type type) {
        return GSON.fromJson(json, type);
    }
    
    /**
     * 解析为 JsonObject
     */
    public static JsonObject parseObject(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }
    
    /**
     * 解析为 JsonArray
     */
    public static JsonArray parseArray(String json) {
        return JsonParser.parseString(json).getAsJsonArray();
    }
    
    /**
     * 获取字符串字段
     */
    public static String getString(JsonObject obj, String key, String defaultValue) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return defaultValue;
    }
    
    public static String getString(JsonObject obj, String key) {
        return getString(obj, key, null);
    }
    
    /**
     * 获取整数字段
     */
    public static int getInt(JsonObject obj, String key, int defaultValue) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsInt();
        }
        return defaultValue;
    }
    
    /**
     * 获取长整数字段
     */
    public static long getLong(JsonObject obj, String key, long defaultValue) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsLong();
        }
        return defaultValue;
    }
    
    /**
     * 获取布尔字段
     */
    public static boolean getBoolean(JsonObject obj, String key, boolean defaultValue) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsBoolean();
        }
        return defaultValue;
    }
    
    /**
     * 获取浮点字段
     */
    public static double getDouble(JsonObject obj, String key, double defaultValue) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsDouble();
        }
        return defaultValue;
    }
    
    /**
     * 获取嵌套对象
     */
    public static JsonObject getObject(JsonObject obj, String key) {
        if (obj.has(key) && obj.get(key).isJsonObject()) {
            return obj.getAsJsonObject(key);
        }
        return null;
    }
    
    /**
     * 获取数组
     */
    public static JsonArray getArray(JsonObject obj, String key) {
        if (obj.has(key) && obj.get(key).isJsonArray()) {
            return obj.getAsJsonArray(key);
        }
        return null;
    }
    
    /**
     * 创建 JsonObject
     */
    public static JsonObject createObject() {
        return new JsonObject();
    }
    
    /**
     * 创建 JsonArray
     */
    public static JsonArray createArray() {
        return new JsonArray();
    }
    
    /**
     * 深拷贝 JsonObject
     */
    public static JsonObject deepCopy(JsonObject source) {
        return GSON.fromJson(source, JsonObject.class);
    }
    
    /**
     * Map 转 JsonObject
     */
    public static JsonObject fromMap(Map<String, ?> map) {
        JsonObject obj = new JsonObject();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value == null) {
                obj.add(entry.getKey(), JsonNull.INSTANCE);
            } else if (value instanceof String) {
                obj.addProperty(entry.getKey(), (String) value);
            } else if (value instanceof Number) {
                obj.addProperty(entry.getKey(), (Number) value);
            } else if (value instanceof Boolean) {
                obj.addProperty(entry.getKey(), (Boolean) value);
            } else if (value instanceof Character) {
                obj.addProperty(entry.getKey(), (Character) value);
            } else {
                obj.add(entry.getKey(), GSON.toJsonTree(value));
            }
        }
        return obj;
    }
    
    /**
     * JsonObject 转 Map
     */
    public static Map<String, Object> toMap(JsonObject obj) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            JsonElement value = entry.getValue();
            if (value.isJsonNull()) {
                map.put(entry.getKey(), null);
            } else if (value.isJsonPrimitive()) {
                JsonPrimitive primitive = value.getAsJsonPrimitive();
                if (primitive.isString()) {
                    map.put(entry.getKey(), primitive.getAsString());
                } else if (primitive.isNumber()) {
                    map.put(entry.getKey(), primitive.getAsNumber());
                } else if (primitive.isBoolean()) {
                    map.put(entry.getKey(), primitive.getAsBoolean());
                }
            } else {
                map.put(entry.getKey(), value);
            }
        }
        return map;
    }
}
