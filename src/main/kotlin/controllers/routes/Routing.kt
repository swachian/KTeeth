package io.github.sw.controllers.routes

import io.github.sw.domain.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.ktor.sse.*
import org.jetbrains.exposed.sql.Database
import org.koin.ktor.ext.inject
import javax.sql.DataSource
import kotlin.getValue

fun Application.configureRouting() {
    val datasource: DataSource by inject()
    val database = Database.connect(
        io.github.sw.db.datasource
    )
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause" , status = HttpStatusCode.InternalServerError)
        }
    }
    install(SSE)

    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        sse("/hello") {
            send(ServerSentEvent("world"))
        }

        val userService = UserService(database)
        val userRoutes = UserRoutes(userService)
        userRoutes.run { configUserRoutes() }
    }
}

