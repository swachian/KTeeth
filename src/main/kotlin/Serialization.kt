package io.github.sw

import com.fasterxml.jackson.databind.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureSerialization() {
    // To serialize responses to json when an object is responsed.
    install(ContentNegotiation) {
        jackson {
//                enable(SerializationFeature.INDENT_OUTPUT)
            }
    }
    routing {
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
        get("/json/jackson") {
                call.respond(mapOf("hello" to "world"))
            }
    }
}
