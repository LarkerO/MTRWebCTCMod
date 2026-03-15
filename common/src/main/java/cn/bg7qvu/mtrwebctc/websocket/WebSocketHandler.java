package cn.bg7qvu.mtrwebctc.websocket;

import cn.bg7qvu.mtrwebctc.config.Config;
import cn.bg7qvu.mtrwebctc.mtr.MTRDataManager;
import cn.bg7qvu.mtrwebctc.mtr.TrainTracker;
import cn.bg7qvu.mtrwebctc.model.TrainDTO;
import cn.bg7qvu.mtrwebctc.util.Logger;
import com.google.gson.Gson;
import io.ktor.server.routing.Routing;
import io.ktor.server.routing.Route;
import io.ktor.server.routing.route;
import io.ktor.server.websocket.WebSocketServerSession;
import io.ktor.server.websocket.webSocket;
import io.ktor.websocket.Frame;
import io.ktor.websocket.readText;

import java.util.*;
import java.util.concurrent.*;

/**
 * WebSocket 处理器
 */
public class WebSocketHandler {
    private final Config config;
    private final MTRDataManager mtrDataManager;
    private final TrainTracker trainTracker;
    private final Gson gson = new Gson();
    
    // 活跃的 WebSocket 连接
    private final Set<io.ktor.websocket.WebSocketSession> connections = ConcurrentHashMap.newKeySet();
    
    // 推送任务调度器
    private final ScheduledExecutorService pushScheduler = Executors.newScheduledThreadPool(1);
    
    public WebSocketHandler(Config config, MTRDataManager mtrDataManager, 
                           TrainTracker trainTracker) {
        this.config = config;
        this.mtrDataManager = mtrDataManager;
        this.trainTracker = trainTracker;
    }
    
    /**
     * 注册路由
     */
    public void register(Routing routing) {
        routing.webSocket("/ws", session -> {
            handleSession(session);
        });
    }
    
    private void handleSession(WebSocketServerSession session) {
        connections.add(session);
        Logger.info("WebSocket client connected, total connections: " + connections.size());
        
        Set<String> subscribedChannels = ConcurrentHashMap.newKeySet();
        
        try {
            // 启动推送任务
            startPushTask(session, subscribedChannels);
            
            // 处理接收的消息
            for (Frame frame : session.getIncoming()) {
                if (frame instanceof Frame.Text) {
                    String text = ((Frame.Text) frame).readText();
                    handleMessage(session, text, subscribedChannels);
                }
            }
        } catch (Exception e) {
            Logger.error("WebSocket error: " + e.getMessage());
        } finally {
            connections.remove(session);
            Logger.info("WebSocket client disconnected, total connections: " + connections.size());
        }
    }
    
    private void handleMessage(io.ktor.websocket.WebSocketSession session, String message, 
                               Set<String> subscribedChannels) {
        try {
            Map<String, Object> msg = gson.fromJson(message, Map.class);
            String action = (String) msg.get("action");
            
            if ("subscribe".equals(action)) {
                List<String> channels = (List<String>) msg.get("channels");
                if (channels != null) {
                    subscribedChannels.addAll(channels);
                    sendAck(session, "subscribed", channels);
                }
            } else if ("unsubscribe".equals(action)) {
                List<String> channels = (List<String>) msg.get("channels");
                if (channels != null) {
                    subscribedChannels.removeAll(channels);
                    sendAck(session, "unsubscribed", channels);
                }
            } else if ("ping".equals(action)) {
                sendPong(session);
            }
        } catch (Exception e) {
            Logger.error("Failed to handle WebSocket message: " + e.getMessage());
        }
    }
    
    private void startPushTask(io.ktor.websocket.WebSocketSession session, Set<String> subscribedChannels) {
        long intervalMs = config.getWebsocket().getPushIntervalMs();
        
        pushScheduler.scheduleAtFixedRate(() -> {
            try {
                if (subscribedChannels.contains("trains")) {
                    pushTrainData(session);
                }
                
                if (subscribedChannels.contains("railway")) {
                    pushRailwayData(session);
                }
            } catch (Exception e) {
                Logger.error("Push error: " + e.getMessage());
            }
        }, 0, intervalMs, TimeUnit.MILLISECONDS);
    }
    
    private void pushTrainData(io.ktor.websocket.WebSocketSession session) {
        Map<String, TrainDTO> trains = trainTracker.getCurrentTrains();
        
        Map<String, Object> message = new HashMap<>();
        message.put("channel", "trains");
        message.put("timestamp", System.currentTimeMillis());
        message.put("data", Collections.singletonMap("trains", trains.values()));
        
        try {
            session.getOutgoing().send(new Frame.Text(gson.toJson(message)));
        } catch (Exception e) {
            Logger.error("Failed to push train data: " + e.getMessage());
        }
    }
    
    private void pushRailwayData(io.ktor.websocket.WebSocketSession session) {
        // TODO: 推送 RailwayData 变化
    }
    
    private void sendAck(io.ktor.websocket.WebSocketSession session, String action, List<String> channels) {
        Map<String, Object> message = new HashMap<>();
        message.put("action", action);
        message.put("channels", channels);
        message.put("timestamp", System.currentTimeMillis());
        
        try {
            session.getOutgoing().send(new Frame.Text(gson.toJson(message)));
        } catch (Exception e) {
            Logger.error("Failed to send ack: " + e.getMessage());
        }
    }
    
    private void sendPong(io.ktor.websocket.WebSocketSession session) {
        Map<String, Object> message = new HashMap<>();
        message.put("action", "pong");
        message.put("timestamp", System.currentTimeMillis());
        
        try {
            session.getOutgoing().send(new Frame.Text(gson.toJson(message)));
        } catch (Exception e) {
            Logger.error("Failed to send pong: " + e.getMessage());
        }
    }
    
    /**
     * 向所有连接广播消息
     */
    public void broadcast(String channel, Object data) {
        Map<String, Object> message = new HashMap<>();
        message.put("channel", channel);
        message.put("timestamp", System.currentTimeMillis());
        message.put("data", data);
        
        String json = gson.toJson(message);
        
        for (io.ktor.websocket.WebSocketSession session : connections) {
            try {
                session.getOutgoing().send(new Frame.Text(json));
            } catch (Exception e) {
                Logger.error("Broadcast error: " + e.getMessage());
            }
        }
    }
    
    /**
     * 停止所有推送任务
     */
    public void stop() {
        pushScheduler.shutdown();
    }
}
