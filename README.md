# KTeeth

This project is about an API server demo written with Kotlin and Ktor. 
It was created using the [Ktor Project Generator](https://start.ktor.io).

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

### Datasource

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

### OpenAPI Doc

Running `.\gradlew buildOpenApi` generates a json file `generated.json` at `build\ktor\openapi`. 
You can tell the ktor to read the json in this way. 
`build\ktor` seems to be one of a couple of prefixes which merge together into resources.

```kotlin
   routing {
        openAPI(path = "openapi", swaggerFile = "openapi/generated.json")
    }
```

This doc doesn't look good. It's not as good as swagger ui or redoc. Maybe I miss something.

### Authentication and authorization 

1. To enable session authentication, at first you need to install sessions. The following code enable `Sessions` and tell 
ktor that session is to be stored in cookie and the key of the cookie.


```kotlin
    install(Sessions) {
    cookie<UserSession>("MY_SESSION") {
        cookie.extensions["SameSite"] = "lax"
        cookie.path = "/"
    }
}

```

Meanwhile, you'd better define a session data class of you own.

```kotlin
@Serializable
data class UserSession(
    val userId: String,
    val role: String
) {
    // secondary constructor
    constructor(accessToken: String) : this(
        userId = extractUserIdFromToken(accessToken),
        role = extractRoleFromToken(accessToken)
    )

    companion object {
        private fun extractUserIdFromToken(token: String): String {
            return token.substringBefore("-") // 示例逻辑
        }

        private fun extractRoleFromToken(token: String): String {
            return token.substringAfter("-") // 示例逻辑
        }
    }
}
```

2. You need an action to create a session. In the project, `loginSession` is used. The action is not pretected by any authentication for 
at the time it is accessed, the user should not have been authenticated.

```kotlin
routing {
    post("/loginSession") {
        val params = call.receiveParameters()
        val username = params["username"] ?: "guest"
        val role = if (username == "admin") "ADMIN" else "USER"
        val session = UserSession(userId = username, role = role)
        call.sessions.set(session)
        call.respondText("Session of ${session.userId} as ${session.role} ")
    }
}
```

3. `authencation session` is enabled to tell ktor how to validate session and what will be return if the validation fails.
In the `challenge`, unauthorized result will be returned. As the project is about an api server demo, it doesn't redirect to another action to ask a user for inputs.

```kotlin
 authentication {
        session<UserSession>(SESSION_AUTH) {
            validate { session ->
                if (session != null) {
                    session
                } else {
                    null
                }
            }

            challenge {
                call.respond(HttpStatusCode.Unauthorized, "Please login first.")
            }
        }
    }
```

4. `authenticate` operation needs adding before actions that need authentication. With the method, a request has to pass the validation of SESSION_AUTH 
before it arrives the action. Since kt is such a DSL friendly language, it's so elegant and smart that the solution is adequate for now.

```kotlin
        authenticate(SESSION_AUTH) {
            get("/private/info") {
                val userSession = call.principal<UserSession>()
                call.respondText("Private info — ${userSession?.userId}.")
            }
        }
```

#### JWT

There are two types of algorithms used in JWT: symmetric and asymmetric encryption.
The symmetric one is HS256, which shares a common secret among all nodes.  
The asymmetric one is RS256, which provides an endpoint to share public key with other nodes. The other nodes need to acquire the public key regularly.

In the project, asymmetric encryption algorithm is used.

When it is issued, it needs to be sent back again in header `Authorization: Bearer <token>`. The ktor jwt module checks the header too.
However, it may be set in cookie as well, only if the server checks the cookie. The standard operation is to put it in the header.

1. The private key will be used in login action.  
2. jwkProvider is used to get a public key which is used to verify the signature in a jwt. 
3. acceptLeeway(3) means that it can accept +-3 secondes time difference.  
4. You can put you business logic in validate lambda.  
5. If the verification or validating fails, the content in challenge will be returned to the client. 

```kotlin
 // Please read the jwt property from the config file if you are using EngineMain
    val privateKeyString = environment.config.property("jwt.privateKey").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()
    val jwtRealm = environment.config.property("jwt.realm").getString()
    val jwkProvider = JwkProviderBuilder(issuer)
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()
    authentication {
        jwt("auth-jwt") {
            realm = jwtRealm
            verifier(jwkProvider, issuer) {
                acceptLeeway(3)
            }
            validate { credential ->
                if (credential.payload.getClaim("username").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
```

In the loginSession, now a jwt can be returned to the client. Meanwhile, a session is set as well.  

```kotlin
post("/loginSession") {
            val params = call.receiveParameters()
            val username = params["username"] ?: "guest"
            val role = if (username == "admin") "ADMIN" else "USER"
            fun makeJwtToken(): String {
                val publicKey = jwkProvider.get("6f8856ed-9189-488f-9011-0ff4b6c08edc").publicKey
                val keySpecPKCS8 = PKCS8EncodedKeySpec(Base64.decode(privateKeyString))
                val privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpecPKCS8)
                val token = JWT.create()
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .withClaim("username",username)
                    .withExpiresAt(Date(System.currentTimeMillis() + 60000))
                    .sign(Algorithm.RSA256(publicKey as RSAPublicKey, privateKey as RSAPrivateKey))
                return token
            }

            val session = UserSession(userId = username, role = role)
            call.sessions.set(session)
            call.respond(hashMapOf("token" to makeJwtToken()))
```

The token generated is like `eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJodHRwOi8vMC4wLjAuMDo4MDgwL2hlbGxvIiwiaXNzIjoiaHR0cDovLzAuMC4wLjA6ODA4MC8iLCJ1c2VybmFtZSI6ImFkbWluIiwiZXhwIjoxNzYwOTQ2NDQzfQ.UvhAWa7zmk8uDzWEOSHegSCra5LWsEsuXVKpWENzhceq7VVNP5V3XnLl0hEWDdUJn3SZjbV1RUqW5ZQKrpPWDg`.
It means `{"alg":"RS256","typ":"JWT"}{"aud":"http://0.0.0.0:8080/hello","iss":"http://0.0.0.0:8080/","username":"admin","exp":1760946443}`

It has been said that when RS256 is used other nodes need to sync the public secret regularly. This can be done with a static plugin. 
`staticFiles(".well-known", File("certs"), "jwks.json")`  

The public json file can be downloaded by the link http://localhost:8080/.well-known/jwks.json 