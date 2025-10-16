package io.github.sw

import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application {
            module()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)

        }
    }

    @Test
    fun testJackson() = testApplication {
        application {
            module()
        }
        client.get("/json/jackson").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("""{"hello":"world"}""", bodyAsText())
        }
    }

}
