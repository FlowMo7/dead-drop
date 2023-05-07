package dev.moetz.deaddrop.plugins

import dev.moetz.deaddrop.template.IndexTemplate
import dev.moetz.deaddrop.template.InfoTemplate
import dev.moetz.deaddrop.template.PickupTemplate
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Application.configure(
    pathPrefix: String?,
    keepFilesTimeInHours: Int,
    showGithubLinkInFooter: Boolean,
    colorCode: String,
    siteTitle: String,
    siteTitleShort: String
) {

    routing {

        get("robots.txt") {
            call.respondText(ContentType.Text.Plain) {
                "User-agent: * Allow: /"
            }
        }

        get("status") {
            call.respondText(ContentType.Text.Plain) {
                "Ok"
            }
        }

        get {
            call.respondHtmlTemplate(
                IndexTemplate(
                    pathPrefix = pathPrefix,
                    showGithubLinkInFooter = showGithubLinkInFooter,
                    colorCode = colorCode,
                    showLinkToInfoPage = true,
                    siteTitle = siteTitle,
                    siteTitleShort = siteTitleShort,
                    keepFilesTimeInHours = keepFilesTimeInHours,
                )
            ) {

            }
        }

        route("pickup") {

            get {
                call.respond(status = HttpStatusCode.NotFound, message = "Not found")
            }

            get("{id}") {
                val dropId = call.parameters["id"]
                call.respondHtmlTemplate(
                    PickupTemplate(
                        pathPrefix = pathPrefix,
                        showGithubLinkInFooter = showGithubLinkInFooter,
                        colorCode = colorCode,
                        showLinkToInfoPage = true,
                        siteTitle = siteTitle,
                        siteTitleShort = siteTitleShort,
                        dropId = dropId,
                    )
                ) {

                }
            }
        }

        get("info") {
            call.respondHtmlTemplate(
                InfoTemplate(
                    pathPrefix = pathPrefix,
                    showGithubLinkInFooter = showGithubLinkInFooter,
                    colorCode = colorCode,
                    showLinkToInfoPage = false,
                    siteTitle = siteTitle,
                    siteTitleShort = siteTitleShort,
                    keepFilesTimeInHours = keepFilesTimeInHours,
                )
            ) {

            }
        }

        get("site.webmanifest") {
            call.respondText(contentType = ContentType.parse("application/manifest+json")) {
                """{"name":"$siteTitle","short_name":"$siteTitleShort","icons":[{"src":"/android-chrome-192x192.png","sizes":"192x192","type":"image/png"},{"src":"/android-chrome-512x512.png","sizes":"512x512","type":"image/png"}],"theme_color":"#$colorCode","background_color":"#ffffff","display":"standalone"}"""
            }
        }

        route("static") {
            resource(remotePath = "frontend.js", resource = "frontend.js")
            resource(remotePath = "drop.js", resource = "drop.js")
            resource(remotePath = "sjcl.js", resource = "sjcl/sjcl.js")

            resource(remotePath = "materialize.min.css", resource = "materialize/materialize.min.css")
            resource(remotePath = "styles.css", resource = "styles.css")
        }

        resource(remotePath = "android-chrome-192x192.png", resource = "icon/android-chrome-192x192.png")
        resource(remotePath = "android-chrome-512x512.png", resource = "icon/android-chrome-512x512.png")
        resource(remotePath = "apple-touch-icon.png", resource = "icon/apple-touch-icon.png")
        resource(remotePath = "favicon.ico", resource = "icon/favicon.ico")
        resource(remotePath = "favicon-16x16.png", resource = "icon/favicon-16x16.png")
        resource(remotePath = "favicon-32x32.png", resource = "icon/favicon-32x32.png")

    }
}
