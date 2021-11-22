package dev.moetz.deaddrop.plugins

import dev.moetz.deaddrop.data.DataRepository
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.configureApi(
    dataRepository: DataRepository,
    isHttps: Boolean,
    domain: String,
) {
    routing {

        route("api") {

            post("drop") {
                try {
                    val content = call.receiveText()
                    val id = dataRepository.addDrop(content)
                    call.respondText(
                        contentType = ContentType.Application.Json,
                        status = HttpStatusCode.OK,
                        text = "{\"pickupUrl\": \"${if (isHttps) "https" else "http"}://$domain/pickup/$id\"}"
                    )
                } catch (throwable: Throwable) {
                    throwable.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }

            get("drop/{id}") {
                try {
                    val id = call.parameters["id"]
                    if (id.isNullOrBlank()) {
                        call.respond(HttpStatusCode.NotFound)
                    } else {
                        val content = dataRepository.getDrop(id)
                        if (content == null) {
                            call.respond(HttpStatusCode.NotFound)
                        } else {
                            call.respondText(
                                contentType = ContentType.Text.Plain,
                                status = HttpStatusCode.OK,
                                text = content
                            )
                        }
                    }
                } catch (throwable: Throwable) {
                    throwable.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }

    }
}
