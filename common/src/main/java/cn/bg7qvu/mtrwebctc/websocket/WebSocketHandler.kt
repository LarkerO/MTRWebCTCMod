package cn.bg7qvu.mtrwebctc.websocket;

import cn.bg7qvu.mtrwebctc.config.Config;
import cn.bg7qvu.mtrwebctc.mtr.TrainTracker;
import cn.bg7qvu.mtrwebctc.model.TrainDTO;
import cn.bg7qvu.mtrwebctc.util.Logger;
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.concurrent.CopyOnWriteArrayList

/**
 * WebSocket 处理器
 */
public class WebSocketHandler(
    private val config: Config,
    private val trainTracker: TrainTracker
) {
    private val connections = CopyOnWriteArrayList<WebSocketSession>()
    
    fun register(route: Route) {
        route.webSocket("/ws") {
            connections.add(this)
            Logger.info("WebSocket connected, total: " + connections.size)
            
            try {
                // 启动推送任务
                val pushJob = launch {
                    while (true) {
                        delay(config.getWebsocket().getPushIntervalMs())
                        pushTrainData()
                    }
                }
                
                // 接收客户端消息
                incoming.consumeEach { frame ->
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        handleMessage(this, text)
                    }
                }
                
                pushJob.cancel()
            } catch (e: Exception) {
                Logger.error("WebSocket error: " + e.message)
            } finally {
                connections.remove(this)
                Logger.info("WebSocket disconnected, total: " + connections.size)
            }
        }
    }
    
    private suspend fun pushTrainData() {
        val trains = trainTracker.getAllTrains()
        val message = mapOf(
            "channel" to "trains",
            "timestamp" to System.currentTimeMillis(),
            "data" to mapOf("trains" to trains)
        )
        
        connections.forEach { session ->
            try {
                session.sendSerialized(message)
            } catch (e: Exception) {
                Logger.error("Failed to push train data: " + e.message)
            }
        }
    }
    
    private suspend fun handleMessage(session: WebSocketSession, message: String) {
        try {
            // 处理订阅请求
            // TODO: 实现 JSON 解析和频道订阅
            session.send("{\"type\":\"ack\",\"message\":\"received\"}")
        } catch (e: Exception) {
            Logger.error("Failed to handle WebSocket message: " + e.message)
        }
    }
    
    public fun broadcast(channel: String, data: Any) {
        val message = mapOf(
            "channel" to channel,
            "timestamp" to System.currentTimeMillis(),
            "data" to data
        )
        
        connections.forEach { session ->
            try {
                launch {
                    session.sendSerialized(message)
                }
            } catch (e: Exception) {
                Logger.error("Failed to broadcast: " + e.message)
            }
        }
    }
    
    public fun getConnectionCount(): Int {
        return connections.size
    }
}
