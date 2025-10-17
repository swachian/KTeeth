package io.github.sw.controllers.routes

import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import javax.sql.DataSource

fun monitorConnectionPool(hikariDataSource: HikariDataSource): Map<String, Any> {
    return try {
        val poolMxBean = hikariDataSource.hikariPoolMXBean

        mapOf(
            "activeConnections" to poolMxBean.activeConnections,
            "idleConnections" to poolMxBean.idleConnections,
            "totalConnections" to poolMxBean.totalConnections,
            "threadsAwaitingConnection" to poolMxBean.threadsAwaitingConnection,
            "maxPoolSize" to hikariDataSource.maximumPoolSize,
            "minIdle" to hikariDataSource.minimumIdle
        )
    } catch (e: Exception) {
        mapOf("error" to e.message)
    } as Map<String, Any>
}

fun Application.configureMonitoringRoutes() {
    val datasource: DataSource by inject()
    routing {
        get("/health") {
            val metrics = mapOf(
                "database" to monitorConnectionPool(datasource as HikariDataSource),
                "timestamp" to System.currentTimeMillis(),
                "status" to "healthy"
            )
            call.respond(metrics)
        }
    }

}