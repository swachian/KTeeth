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
| [Authentication](https://start.ktor.io/p/auth)                         | Provides extension point for handling the Authorization header                     |
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
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

