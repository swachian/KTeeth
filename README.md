# KTeeth

This project was created using the [Ktor Project Generator](https://start.ktor.io).

Here are some useful links to get you started:

- [Ktor Documentation](https://ktor.io/docs/home.html)

## Features

Here's a list of features included in this project:

| Name                                                                   | Description                                                                        |
| ------------------------------------------------------------------------|------------------------------------------------------------------------------------ |
| [Content Negotiation](https://start.ktor.io/p/content-negotiation)     | Provides automatic content conversion according to Content-Type and Accept headers |
| [Routing](https://start.ktor.io/p/routing)                             | Provides a structured routing DSL                                                  |
| [kotlinx.serialization](https://start.ktor.io/p/kotlinx-serialization) | Handles JSON serialization using kotlinx.serialization library                     |
| [Call Logging](https://start.ktor.io/p/call-logging)                   | Logs client requests                                                               |
| [Koin](https://start.ktor.io/p/koin)                                   | Provides dependency injection                                                      |
| [Kafka](https://start.ktor.io/p/ktor-server-kafka)                     | Adds Kafka support to your application                                             |
| [Exposed](https://start.ktor.io/p/exposed)                             | Adds Exposed database to your application                                          |
| [Jackson](https://start.ktor.io/p/ktor-jackson)                        | Handles JSON serialization using Jackson library                                   |
| [Metrics](https://start.ktor.io/p/metrics)                             | Adds supports for monitoring several metrics                                       |
| [Call ID](https://start.ktor.io/p/callid)                              | Allows to identify a request/call.                                                 |
| [Status Pages](https://start.ktor.io/p/status-pages)                   | Provides exception handling for routes                                             |
| [Server-Sent Events (SSE)](https://start.ktor.io/p/sse)                | Support for server push events                                                     |
| [Default Headers](https://start.ktor.io/p/default-headers)             | Adds a default set of headers to HTTP responses                                    |
| [OpenAPI](https://start.ktor.io/p/openapi)                             | Serves OpenAPI documentation                                                       |
| [Authentication](https://start.ktor.io/p/auth)                         | Provides extension point for handling the Authorization header                     |
| [Authentication Basic](https://start.ktor.io/p/auth-basic)             | Handles 'Basic' username / password authentication scheme                          |
| [Sessions](https://start.ktor.io/p/ktor-sessions)                      | Adds support for persistent sessions through cookies or headers                    |
| [CSRF](https://start.ktor.io/p/csrf)                                   | Cross-site request forgery mitigation                                              |
| [Authentication OAuth](https://start.ktor.io/p/auth-oauth)             | Handles OAuth Bearer authentication scheme                                         |
| [Authentication JWT](https://start.ktor.io/p/auth-jwt)                 | Handles JSON Web Token (JWT) bearer authentication scheme                          |

## Building & Running

To build or run the project, use one of the following tasks:

| Task                                    | Description                                                          |
| -----------------------------------------|---------------------------------------------------------------------- |
| `./gradlew test`                        | Run the tests                                                        |
| `./gradlew build`                       | Build everything                                                     |
| `./gradlew buildFatJar`                 | Build an executable JAR of the server with all dependencies included |
| `./gradlew buildImage`                  | Build the docker image to use with the fat JAR                       |
| `./gradlew publishImageToLocalRegistry` | Publish the docker image locally                                     |
| `./gradlew run`                         | Run the server                                                       |
| `./gradlew runDocker`                   | Run using the local docker image                                     |

If the server starts successfully, you'll see the following output:

```
2025-10-16 15:31:02.442 [main]  INFO  Application - Application started in 1.853 seconds.
2025-10-16 15:31:02.457 [main]  INFO  org.eclipse.jetty.server.Server - jetty-12.0.25; built: 2025-08-11T23:52:37.219Z; git: a862b76d8372e24205765182d9ae1d1d333ce2ea; jvm 24.0.2+12-54
2025-10-16 15:31:02.495 [main]  INFO  o.e.jetty.server.AbstractConnector - Started ServerConnector@274bae2c{HTTP/1.1, (http/1.1, h2c)}{0.0.0.0:8080}
2025-10-16 15:31:02.502 [main]  INFO  org.eclipse.jetty.server.Server - Started oejs.Server@50ff7063{STARTING}[12.0.25,sto=0] @2525ms
2025-10-16 15:31:11.205 [metrics-logger-reporter-1-thread-1]  INFO  Application - type=GAUGE, name=jvm.attributes.name, value=16292@MinisForum870
20
```

## Traps

The version file is located at gradle/libs.versions.toml so that all versions can be written to one file.  
In the `build.gradle.kts` file, ` implementation(libs.ktor.server.content.negotiation)` means fetch the info from the libs.  
There is a little surprise.

```kotlin
application {
//    mainClass = "io.ktor.server.jetty.jakarta.EngineMain"
    mainClass = "io.github.sw.ApplicationKt"
}
```
Both of them can serve as mainClass statement. The modules need loading is defined in application.yml  
```yaml
ktor:
  application:
    modules:
      - io.github.sw.ApplicationKt.module
```

Modules are defined with extension funs.   
```kotlin
fun Application.configureRouting() {
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
    }
}
```

Serialization use extension function as well.
```kotlin
fun Application.configureSerialization() {
    // To serialize responses to json when an object is responsed.
    install(ContentNegotiation) {
        jackson {
                enable(SerializationFeature.INDENT_OUTPUT)
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
```

Use mysql with a connection pool instead of h2 in the project. In order to monit the status of the connection pool, add an action `/health`. As the action is in another kt file, datasource has to be exposed to the other file. KOIN is used. 
```kotlin
fun Application.configureFrameworks() {
    install(Koin) {
        slf4jLogger()
        modules(module {
            single<HelloService> {
                HelloService {
                    println(environment.log.info("Hello, World!"))
                }
            }
            // register datasource
            single { datasource }
        })
    }
}

fun Application.configureMonitoringRoutes() {
    // inject the datasource
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
```

### Routes

UserService is injected into UserRoutes. An extension function of Root is declared in the UserRoutes for it will use 
UserService. However, as the function is defined in the class, it can only be called in the scope of the instance of UserRoutes.

To do so, a standard high order function in the stdlib, `run` is used in the common routing file to call `configUserRoutes` 
of userRoutines instance.


```kotlin
class UserRoutes(val userService: UserService) {
    fun Route.configUserRoutes() {

        route("/users") {
            get {
                val users = userService.read()
                call.respond(users)
            }

        }
    }
}

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
```

### About the DB and db driver

Unfortunately, Exposed or Ktorm is based on JDBC so they are not coroutine friendly. Although R2DBC has been designed 
for async IO, there hasn't been any ORM framework based on R2DBC now. Currently, we have to stick to JDBC, a synchronized db driver.

### Call Logging and Call Id

The second statement `generate` has to be added, or if there is no request id from a client, call id is missing and it won't
wash with logging.  

```kotlin
    install(CallId) {
        header(HttpHeaders.XRequestId)
        generate { java.util.UUID.randomUUID().toString() }
        verify { it.isNotEmpty() }
    }
    install(CallLogging) {
        callIdMdc("call-id")
    }
```

```text
2025-10-17 20:36:24.067 [ktor-jetty-8080-5] aeeeb4b7-d6bd-4688-992e-6e0cf1bd2325 INFO  Application - 200 OK: GET - /users in 257ms
```

### SSE

This is a technology keeping connections between a client and server. However, unlike websocket, only the server side can 
send msg to the client. In the years without NIO, it's almost impossible to keep so many connections. Now it's easier for 
a server to keep in touch with clients than past, for ktor uses coroutines and NIO. Many fds still need opening.


### Hot Reload

If the idea is running in debug mode, Rebuild the file changed can reload the changed class to JVM.

