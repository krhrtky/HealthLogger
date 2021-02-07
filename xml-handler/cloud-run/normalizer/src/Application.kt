package com.healthlogger.normalizer

import com.fasterxml.jackson.databind.SerializationFeature
import com.healthlogger.normalizer.externals.di.DIContainer
import com.healthlogger.normalizer.routes.normalizer
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.routing.routing
import org.koin.core.logger.Level as KoinLoggerLevel
import org.koin.ktor.ext.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.event.Level

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    install(CallLogging) {
        level = Level.DEBUG
    }

    install(Koin) {
        slf4jLogger(KoinLoggerLevel.DEBUG)
        modules(DIContainer.normalizer)
    }

    routing {
        normalizer()
    }
}
