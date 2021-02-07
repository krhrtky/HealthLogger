package com.healthlogger.normalizer.routes

import com.healthlogger.normalizer.controllers.NormalizerController
import com.healthlogger.normalizer.usecases.stepCount.Normalizer
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import org.koin.java.KoinJavaComponent.get

data class FilePath(val filePath: String)


fun Route.normalizer() {
    val controller = get(NormalizerController::class.java)

    post("/normalize") {
        val body = call.receive<FilePath>()

        runCatching {
            controller.normalize(body.filePath)
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
