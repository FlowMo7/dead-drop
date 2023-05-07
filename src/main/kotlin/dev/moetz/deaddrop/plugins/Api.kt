package dev.moetz.deaddrop.plugins

import dev.moetz.deaddrop.combinePartsToFullUrl
import dev.moetz.deaddrop.data.DataRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureApi(
    dataRepository: DataRepository,
    isHttps: Boolean,
    domain: String,
    pathPrefix: String?,
) {
    routing {

        route("api") {

            post("drop") {
                try {
                    val content = call.receiveText()
                    val id = dataRepository.addDrop(content)

                    val pickupUrl = combinePartsToFullUrl(isHttps, domain, pathPrefix) + "pickup/$id"

                    call.respondText(
                        contentType = ContentType.Application.Json,
                        status = HttpStatusCode.OK,
                        text = "{\"id\":\"$id\",\"pickupUrl\":\"$pickupUrl\"}"
                    )
                } catch (throwable: Throwable) {
                    throwable.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }

            route("drop") {

                get {
                    call.respond(status = HttpStatusCode.NotFound, message = "Not found")
                }

                get("{id}") {
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
}
