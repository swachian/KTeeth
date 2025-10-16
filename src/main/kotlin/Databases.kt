package io.github.sw

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.codahale.metrics.*
import com.fasterxml.jackson.databind.*
import io.github.flaxoos.ktor.server.plugins.kafka.Kafka
import io.github.flaxoos.ktor.server.plugins.kafka.MessageTimestampType
import io.github.flaxoos.ktor.server.plugins.kafka.TopicName
import io.github.flaxoos.ktor.server.plugins.kafka.admin
import io.github.flaxoos.ktor.server.plugins.kafka.common
import io.github.flaxoos.ktor.server.plugins.kafka.consumer
import io.github.flaxoos.ktor.server.plugins.kafka.consumerConfig
import io.github.flaxoos.ktor.server.plugins.kafka.consumerRecordHandler
import io.github.flaxoos.ktor.server.plugins.kafka.producer
import io.github.flaxoos.ktor.server.plugins.kafka.registerSchemas
import io.github.flaxoos.ktor.server.plugins.kafka.topic
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.metrics.dropwizard.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.csrf.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.sse.*
import io.ktor.sse.*
import java.util.concurrent.TimeUnit
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.event.*

fun Application.configureDatabases() {
    install(Kafka) {
        schemaRegistryUrl = "my.schemaRegistryUrl"
        val myTopic = TopicName.named("my-topic")
        topic(myTopic) {
            partitions = 1
            replicas = 1
            configs {
                messageTimestampType = MessageTimestampType.CreateTime
            }
        }
        common { // <-- Define common properties
            bootstrapServers = listOf("my-kafka")
            retries = 1
            clientId = "my-client-id"
        }
        admin { } // <-- Creates an admin client
        producer { // <-- Creates a producer
            clientId = "my-client-id"
        }
        consumer { // <-- Creates a consumer
            groupId = "my-group-id"
            clientId = "my-client-id-override" //<-- Override common properties
        }
        consumerConfig {
            consumerRecordHandler(myTopic) { record ->
                // Do something with record
            }
        }
        registerSchemas {
            using { // <-- optionally provide a client, by default CIO is used
                HttpClient()
            }
            // MyRecord::class at myTopic // <-- Will register schema upon startup
        }
    }
    val database = Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        user = "root",
        driver = "org.h2.Driver",
        password = "",
    )
    val userService = UserService(database)
    routing {
        // Create user
        post("/users") {
            val user = call.receive<ExposedUser>()
            val id = userService.create(user)
            call.respond(HttpStatusCode.Created, id)
        }
        
        // Read user
        get("/users/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val user = userService.read(id)
            if (user != null) {
                call.respond(HttpStatusCode.OK, user)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        
        // Update user
        put("/users/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val user = call.receive<ExposedUser>()
            userService.update(id, user)
            call.respond(HttpStatusCode.OK)
        }
        
        // Delete user
        delete("/users/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            userService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
}
