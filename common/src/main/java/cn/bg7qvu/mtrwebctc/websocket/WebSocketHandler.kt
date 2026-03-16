package cn.bg7qvu.mtrwebctc.websocket

import cn.bg7qvu.mtrwebctc.config.Config
import cn.bg7qvu.mtrwebctc.mtr.MTRDataManager
import cn.bg7qvu.mtrwebctc.mtr.TrainTracker
import cn.bg7qvu.mtrwebctc.util.Logger
import com.google.gson.Gson
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.util.concurrent.*

class WebSocketHandler(
    private val config: Config,
    private val mtrDataManager: MTRDataManager,
    private val trainTracker: TrainTracker
) {
    private val connections: MutableSet<WebSocketSession> = ConcurrentHashMap.newKeySet()
    private val gson = Gson()
    private val pushScheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    fun register(routing: Route) {
        routing.webSocket("/ws") {
            connections += this
            Logger.info("WebSocket client connected, total connections: ${connections.size}")

            val subscribedChannels: MutableSet<String> = ConcurrentHashMap.newKeySet()

            startPushTask(this, subscribedChannels)

            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        handleMessage(this, frame.readText(), subscribedChannels)
                    }
                }
            } catch (e: Exception) {
                Logger.error("WebSocket error: ${e.message}")
            } finally {
                connections -= this
                Logger.info("WebSocket client disconnected, total connections: ${connections.size}")
            }
        }
    }

    private fun handleMessage(
        session: WebSocketSession,
        message: String,
        subscribedChannels: MutableSet<String>
    ) {
        try {
            @Suppress("UNCHECKED_CAST")
            val msg = gson.fromJson(message, Map::class.java) as Map<String, Any>
            val action = msg["action"] as? String

            when (action) {
                "subscribe" -> {
                    @Suppress("UNCHECKED_CAST")
                    val channels = msg["channels"] as? List<String>
                    if (channels != null) {
                        subscribedChannels.addAll(channels)
                        sendAck(session, "subscribed", channels)
                    }
                }
                "unsubscribe" -> {
                    @Suppress("UNCHECKED_CAST")
                    val channels = msg["channels"] as? List<String>
                    if (channels != null) {
                        channels.forEach { subscribedChannels.remove(it) }
                        sendAck(session, "unsubscribed", channels)
                    }
                }
                "ping" -> sendPong(session)
            }
        } catch (e: Exception) {
            Logger.error("Failed to handle WebSocket message: ${e.message}")
        }
    }

    private fun startPushTask(session: WebSocketSession, subscribedChannels: Set<String>) {
        val intervalMs = config.websocket.pushIntervalMs

        pushScheduler.scheduleAtFixedRate({
            try {
                if ("trains" in subscribedChannels) {
                    pushTrainData(session)
                }
                if ("railway" in subscribedChannels) {
                    pushRailwayData(session)
                }
            } catch (e: Exception) {
                Logger.error("Push error: ${e.message}")
            }
        }, 0, intervalMs, TimeUnit.MILLISECONDS)
    }

    private fun pushTrainData(session: WebSocketSession) {
        val trains = trainTracker.allTrains

        val message = mapOf(
            "channel" to "trains",
            "timestamp" to System.currentTimeMillis(),
            "data" to mapOf("trains" to trains)
        )
        try {
            session.outgoing.trySend(Frame.Text(gson.toJson(message)))
        } catch (e: Exception) {
            Logger.error("Failed to push train data: ${e.message}")
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun pushRailwayData(session: WebSocketSession) {
        // TODO: push RailwayData changes
    }

    private fun sendAck(session: WebSocketSession, action: String, channels: List<String>) {
        val message = mapOf(
            "action" to action,
            "channels" to channels,
            "timestamp" to System.currentTimeMillis()
        )
        try {
            session.outgoing.trySend(Frame.Text(gson.toJson(message)))
        } catch (e: Exception) {
            Logger.error("Failed to send ack: ${e.message}")
        }
    }

    private fun sendPong(session: WebSocketSession) {
        val message = mapOf(
            "action" to "pong",
            "timestamp" to System.currentTimeMillis()
        )
        try {
            session.outgoing.trySend(Frame.Text(gson.toJson(message)))
        } catch (e: Exception) {
            Logger.error("Failed to send pong: ${e.message}")
        }
    }

    fun broadcast(channel: String, data: Any) {
        val message = mapOf(
            "channel" to channel,
            "timestamp" to System.currentTimeMillis(),
            "data" to data
        )
        val json = gson.toJson(message)

        for (session in connections) {
            try {
                session.outgoing.trySend(Frame.Text(json))
            } catch (e: Exception) {
                Logger.error("Broadcast error: ${e.message}")
            }
        }
    }

    fun stop() {
        pushScheduler.shutdown()
    }
}
