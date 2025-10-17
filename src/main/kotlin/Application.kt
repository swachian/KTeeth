package io.github.sw

import io.github.sw.controllers.routes.configureHTTP
import io.github.sw.controllers.routes.configureMonitoringRoutes
import io.github.sw.controllers.routes.configureRouting
import io.github.sw.db.configureDatabases
import io.github.sw.infras.configureMonitoring
import io.github.sw.infras.configureSecurity
import io.github.sw.infras.configureSerialization
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.jetty.jakarta.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureSecurity()
    configureFrameworks()
    configureDatabases()
    configureHTTP()
    configureRouting()
    configureMonitoringRoutes()
}
