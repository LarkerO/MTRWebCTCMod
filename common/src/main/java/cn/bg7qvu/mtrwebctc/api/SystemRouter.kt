package cn.bg7qvu.mtrwebctc.api

import cn.bg7qvu.mtrwebctc.util.Constants
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class SystemRouter {

    fun register(routing: Route) {
        routing.route("/system") {
            get("info") {
                val runtime = Runtime.getRuntime()
                val info = linkedMapOf<String, Any>(
                    "name" to Constants.MOD_NAME,
                    "version" to Constants.MOD_VERSION,
                    "java" to System.getProperty("java.version"),
                    "os" to System.getProperty("os.name"),
                    "arch" to System.getProperty("os.arch"),
                    "maxMemory" to runtime.maxMemory(),
                    "totalMemory" to runtime.totalMemory(),
                    "freeMemory" to runtime.freeMemory(),
                    "usedMemory" to (runtime.totalMemory() - runtime.freeMemory()),
                    "processors" to runtime.availableProcessors(),
                    "timestamp" to System.currentTimeMillis(),
                    "uptime" to getUptimeMs()
                )
                call.respond(info)
            }

            get("health") {
                val runtime = Runtime.getRuntime()
                val usedMemory = runtime.totalMemory() - runtime.freeMemory()
                val maxMemory = runtime.maxMemory()
                val memoryUsage = usedMemory.toDouble() / maxMemory * 100

                call.respond(mapOf(
                    "status" to "healthy",
                    "timestamp" to System.currentTimeMillis(),
                    "memoryUsage" to "%.2f%%".format(memoryUsage),
                    "memoryHealthy" to (memoryUsage < 90)
                ))
            }

            get("version") {
                call.respond(mapOf(
                    "modVersion" to Constants.MOD_VERSION,
                    "modName" to Constants.MOD_NAME,
                    "modId" to Constants.MOD_ID
                ))
            }

            get("stats") {
                val runtime = Runtime.getRuntime()
                call.respond(mapOf(
                    "memory" to mapOf(
                        "max" to runtime.maxMemory(),
                        "total" to runtime.totalMemory(),
                        "free" to runtime.freeMemory(),
                        "used" to (runtime.totalMemory() - runtime.freeMemory())
                    ),
                    "threads" to mapOf(
                        "active" to Thread.activeCount(),
                        "peak" to Thread.activeCount() // TODO: track peak
                    ),
                    "timestamp" to System.currentTimeMillis(),
                    "uptimeMs" to getUptimeMs()
                ))
            }
        }
    }

    private fun getUptimeMs(): Long {
        // TODO: get actual start time from MTRWebCTCMod
        return System.currentTimeMillis()
    }
}
