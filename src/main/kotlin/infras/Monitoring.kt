package io.github.sw.infras

import com.codahale.metrics.*
import io.ktor.http.*
import io.ktor.server.application.*

import io.ktor.server.metrics.dropwizard.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.calllogging.*
import java.util.concurrent.TimeUnit


fun Application.configureMonitoring() {
    install(DropwizardMetrics) {
        Slf4jReporter.forRegistry(registry)
            .outputTo(this@configureMonitoring.log)
//            .convertRatesTo(TimeUnit.SECONDS)
//            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build()
            .start(2, TimeUnit.MINUTES)
    }
    install(CallId) {
        header(HttpHeaders.XRequestId)
        generate { java.util.UUID.randomUUID().toString() }
        verify { it.isNotEmpty() }
    }
    install(CallLogging) {
        callIdMdc("call-id")
    }
}
