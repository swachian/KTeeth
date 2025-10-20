package io.github.sw.controllers.auth

import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.github.sw.controllers.auth.AuthTypeConsts.BASIC_AUTH
import io.github.sw.controllers.auth.AuthTypeConsts.SESSION_AUTH
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.http.content.staticFiles
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable
import java.io.File
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.io.encoding.Base64

object AuthTypeConsts {
    const val BASIC_AUTH = "basic-auth"
    const val SESSION_AUTH = "session-auth"
}
fun Application.configureSecurity() {
    authentication {
        basic(name = BASIC_AUTH) {
            realm = "Ktor Server"
            validate { credentials ->
                if (credentials.name == "ddd" && credentials.password == "ddd") {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    
        form(name = "myauth2") {
            userParamName = "user"
            passwordParamName = "password"
            challenge {
                /**/
            }
        }
    }
    install(Sessions) {
        cookie<UserSession>("MY_SESSION") {
            cookie.extensions["SameSite"] = "lax"
            cookie.path = "/"
        }
    }
/*    install(CSRF) {
        // tests Origin is an expected value
        allowOrigin("http://localhost:8080")
    
        // tests Origin matches Host header
        originMatchesHost()
    
        // custom header checks
        checkHeader("X-CSRF-Token")

        onFailure {
            respond(HttpStatusCode.Forbidden, "CSRF token invalid")
        }
    }*/
    authentication {
        oauth("auth-oauth-google") {
            urlProvider = { "http://localhost:8080/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "google",
                    authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
                    accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
                    requestMethod = HttpMethod.Post,
                    clientId = System.getenv("GOOGLE_CLIENT_ID"),
                    clientSecret = System.getenv("GOOGLE_CLIENT_SECRET"),
                    defaultScopes = listOf("https://www.googleapis.com/auth/userinfo.profile")
                )
            }
            client = HttpClient(Apache)
        }
    }
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
    routing {
        authenticate(BASIC_AUTH) {
            get("/protected/route/basic") {
                val principal = call.principal<UserIdPrincipal>()!!
                call.respondText("Hello ${principal.name}")
            }
        }
        authenticate("myauth2") {
            get("/protected/route/form") {
                val principal = call.principal<UserIdPrincipal>()!!
                call.respondText("Hello ${principal.name}")
            }
        }


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
        }

        staticFiles(".well-known", File("certs"), "jwks.json")

        authenticate("auth-oauth-google") {
            get("login") {
                call.respondRedirect("/callback")
            }
        
            get("/callback") {
                val principal: OAuthAccessTokenResponse.OAuth2? = call.authentication.principal()
                call.sessions.set(UserSession(principal?.accessToken.toString()))
                call.respondRedirect("/hello")
            }
        }
    }
}

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