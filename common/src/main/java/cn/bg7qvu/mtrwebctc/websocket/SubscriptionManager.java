package cn.bg7qvu.mtrwebctc.websocket;

import cn.bg7qvu.mtrwebctc.util.Logger;
import io.ktor.websocket.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * WebSocket 订阅管理器
 * 管理客户端订阅的频道
 */
public class SubscriptionManager {
    // 频道 -> 订阅的 WebSocket 会话
    private final Map<String, Set<WebSocketSession>> channelSubscriptions = new ConcurrentHashMap<>();
    
    // WebSocket 会话 -> 订阅的频道
    private final Map<WebSocketSession, Set<String>> sessionSubscriptions = new ConcurrentHashMap<>();
    
    // Supported channels
    private static final Set<String> ALLOWED_CHANNELS;
    static {
        Set<String> channels = new HashSet<>();
        channels.add("trains");
        channels.add("stations");
        channels.add("routes");
        channels.add("depots");
        channels.add("schedules");
        channels.add("alerts");
        channels.add("system");
        ALLOWED_CHANNELS = Collections.unmodifiableSet(channels);
    }
    
    /**
     * 订阅频道
     */
    public boolean subscribe(WebSocketSession session, String channel) {
        if (!ALLOWED_CHANNELS.contains(channel)) {
            Logger.warn("Invalid channel: " + channel);
            return false;
        }
        
        channelSubscriptions.computeIfAbsent(channel, k -> ConcurrentHashMap.newKeySet())
                            .add(session);
        sessionSubscriptions.computeIfAbsent(session, k -> ConcurrentHashMap.newKeySet())
                            .add(channel);
        
        Logger.debug("Session subscribed to: " + channel);
        return true;
    }
    
    /**
     * 取消订阅频道
     */
    public boolean unsubscribe(WebSocketSession session, String channel) {
        Set<WebSocketSession> subscribers = channelSubscriptions.get(channel);
        if (subscribers != null) {
            subscribers.remove(session);
        }
        
        Set<String> channels = sessionSubscriptions.get(session);
        if (channels != null) {
            channels.remove(channel);
        }
        
        Logger.debug("Session unsubscribed from: " + channel);
        return true;
    }
    
    /**
     * 取消会话的所有订阅
     */
    public void unsubscribeAll(WebSocketSession session) {
        Set<String> channels = sessionSubscriptions.remove(session);
        if (channels != null) {
            for (String channel : channels) {
                Set<WebSocketSession> subscribers = channelSubscriptions.get(channel);
                if (subscribers != null) {
                    subscribers.remove(session);
                }
            }
        }
        Logger.debug("Session unsubscribed from all channels");
    }
    
    /**
     * 获取频道的订阅者
     */
    public Set<WebSocketSession> getSubscribers(String channel) {
        return Collections.unmodifiableSet(
            channelSubscriptions.getOrDefault(channel, Collections.emptySet())
        );
    }
    
    /**
     * 获取会话订阅的频道
     */
    public Set<String> getSubscribedChannels(WebSocketSession session) {
        return Collections.unmodifiableSet(
            sessionSubscriptions.getOrDefault(session, Collections.emptySet())
        );
    }
    
    /**
     * 检查是否已订阅
     */
    public boolean isSubscribed(WebSocketSession session, String channel) {
        Set<String> channels = sessionSubscriptions.get(session);
        return channels != null && channels.contains(channel);
    }
    
    /**
     * 获取频道订阅数量
     */
    public int getSubscriberCount(String channel) {
        Set<WebSocketSession> subscribers = channelSubscriptions.get(channel);
        return subscribers != null ? subscribers.size() : 0;
    }
    
    /**
     * 获取所有支持的频道
     */
    public Set<String> getAllowedChannels() {
        return ALLOWED_CHANNELS;
    }
    
    /**
     * 清理断开的会话
     */
    public void cleanup(WebSocketSession session) {
        unsubscribeAll(session);
    }
    
    /**
     * 获取统计信息
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalSessions", sessionSubscriptions.size());
        
        Map<String, Integer> channelStats = new LinkedHashMap<>();
        for (String channel : ALLOWED_CHANNELS) {
            channelStats.put(channel, getSubscriberCount(channel));
        }
        stats.put("channels", channelStats);
        
        return stats;
    }
}
