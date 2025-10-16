package io.github.sw

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
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
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*

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
    val host = System.getenv("DB_HOST") ?: "localhost"
    val port = System.getenv("DB_PORT") ?: 3306
    val dbName = System.getenv("DB_NAME") ?:  "kteeth"
    val dbUser = System.getenv("DB_USER") ?: "root"
    val dbPassword = System.getenv("DB_PASSWORD") ?: "root"
    val dbConfig = environment.config.config("ktor.database")
    val hikariConfig = HikariConfig().apply {
        jdbcUrl = "jdbc:mysql://$host:$port/$dbName"
        username = dbUser
        password = dbPassword
        driverClassName = "com.mysql.cj.jdbc.Driver"

        // 连接池配置
        maximumPoolSize = dbConfig.property("pool.maximumPoolSize").getString().toInt()
        minimumIdle = dbConfig.property("pool.minimumIdle").getString().toInt()
        maxLifetime = dbConfig.property("pool.maxLifetime").getString().toLong()
        connectionTimeout = dbConfig.property("pool.connectionTimeout").getString().toLong()
        idleTimeout = dbConfig.property("pool.idleTimeout").getString().toLong()
        validationTimeout = dbConfig.property("pool.validationTimeout").getString().toLong()
        leakDetectionThreshold = dbConfig.property("pool.leakDetectionThreshold").getString().toLong()

        connectionTestQuery = "SELECT 1"

        // MySQL 优化属性
        val properties = dbConfig.config("properties")
        properties.keys().forEach { key ->
            addDataSourceProperty(key, properties.property(key).getString())
        }
    }
    val database = Database.connect(
        HikariDataSource(hikariConfig)
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
