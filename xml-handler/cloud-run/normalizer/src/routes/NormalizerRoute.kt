package com.healthlogger.normalizer.routes

import com.healthlogger.normalizer.controllers.NormalizerController
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import org.koin.java.KoinJavaComponent.get
import java.util.Base64

fun Route.normalizer() {
    val controller = get(NormalizerController::class.java)

    post("/normalize") {
        call.application.environment.log.info("Start normalize.")

        val filePath  = try {
            val body = call.receive<Body>()
            String(Base64.getDecoder().decode(body.message.data))

        } catch (e: Exception) {
            call.application.environment.log.error("Fail convert file.", e)
            return@post call.respond(HttpStatusCode.InternalServerError, "Fail execute")
        }

        runCatching {
            controller.normalize(filePath)
        }.fold(
            onSuccess = {
             call.respond(HttpStatusCode.OK)
            },
            onFailure = {
                call.application.environment.log.error("Fail convert file.", it)
                call.respond(HttpStatusCode.InternalServerError)
            }
        )
    }
}
