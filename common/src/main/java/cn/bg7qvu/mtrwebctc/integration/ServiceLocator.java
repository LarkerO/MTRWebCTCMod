package cn.bg7qvu.mtrwebctc.integration;

import cn.bg7qvu.mtrwebctc.util.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务定位器
 * 用于管理服务实例
 */
public class ServiceLocator {
    private static final Map<Class<?>, Object> services = new ConcurrentHashMap<>();
    private static final Map<String, Object> namedServices = new ConcurrentHashMap<>();
    
    private ServiceLocator() {}
    
    /**
     * 注册服务
     * @param serviceClass 服务接口类
     * @param service 实现实例
     */
    public static <T> void register(Class<T> serviceClass, T service) {
        if (serviceClass == null || service == null) {
            throw new IllegalArgumentException("Service class and instance cannot be null");
        }
        
        services.put(serviceClass, service);
        Logger.debug("Registered service: " + serviceClass.getSimpleName());
    }
    
    /**
     * 注册命名服务
     * @param name 服务名称
     * @param service 服务实例
     */
    public static void register(String name, Object service) {
        if (name == null || service == null) {
            throw new IllegalArgumentException("Name and service cannot be null");
        }
        
        namedServices.put(name, service);
        Logger.debug("Registered named service: " + name);
    }
    
    /**
     * 获取服务
     * @param serviceClass 服务接口类
     * @return 服务实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> serviceClass) {
        T service = (T) services.get(serviceClass);
        if (service == null) {
            throw new IllegalStateException("Service not registered: " + serviceClass.getSimpleName());
        }
        return service;
    }
    
    /**
     * 获取服务，如果不存在返回 null
     * @param serviceClass 服务接口类
     * @return 服务实例或 null
     */
    @SuppressWarnings("unchecked")
    public static <T> T getOrNull(Class<T> serviceClass) {
        return (T) services.get(serviceClass);
    }
    
    /**
     * 获取命名服务
     * @param name 服务名称
     * @return 服务实例
     */
    public static Object get(String name) {
        Object service = namedServices.get(name);
        if (service == null) {
            throw new IllegalStateException("Named service not registered: " + name);
        }
        return service;
    }
    
    /**
     * 获取命名服务，如果不存在返回 null
     * @param name 服务名称
     * @return 服务实例或 null
     */
    public static Object getOrNull(String name) {
        return namedServices.get(name);
    }
    
    /**
     * 检查服务是否已注册
     * @param serviceClass 服务接口类
     * @return 是否已注册
     */
    public static boolean has(Class<?> serviceClass) {
        return services.containsKey(serviceClass);
    }
    
    /**
     * 检查命名服务是否已注册
     * @param name 服务名称
     * @return 是否已注册
     */
    public static boolean has(String name) {
        return namedServices.containsKey(name);
    }
    
    /**
     * 注销服务
     * @param serviceClass 服务接口类
     */
    public static <T> void unregister(Class<T> serviceClass) {
        services.remove(serviceClass);
        Logger.debug("Unregistered service: " + serviceClass.getSimpleName());
    }
    
    /**
     * 注销命名服务
     * @param name 服务名称
     */
    public static void unregister(String name) {
        namedServices.remove(name);
        Logger.debug("Unregistered named service: " + name);
    }
    
    /**
     * 清空所有服务
     */
    public static void clear() {
        services.clear();
        namedServices.clear();
        Logger.debug("All services cleared");
    }
    
    /**
     * 获取已注册的服务数量
     * @return 服务数量
     */
    public static int size() {
        return services.size() + namedServices.size();
    }
    
    /**
     * 获取所有已注册的服务类
     * @return 服务类集合
     */
    public static Set<Class<?>> registeredServices() {
        return new HashSet<>(services.keySet());
    }
    
    /**
     * 获取所有已注册的服务名称
     * @return 服务名称集合
     */
    public static Set<String> registeredNames() {
        return new HashSet<>(namedServices.keySet());
    }
}
