package dev.moetz.deaddrop.plugins

import dev.moetz.deaddrop.Localization
import dev.moetz.deaddrop.Shynet
import dev.moetz.deaddrop.template.IndexTemplate
import dev.moetz.deaddrop.template.PickupTemplate
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*


fun PipelineContext<Unit, ApplicationCall>.localization(): Localization {
    val languageFromHlQueryParameter = call.request.queryParameters["hl"]
        ?.takeIf { it.isNotBlank() }
        ?.let { hl ->
            Localization.Language.entries.firstOrNull { language ->
                language.identifier.equals(hl, ignoreCase = true)
            }
        }

    val languageFromAcceptLanguageHeader = call.request.acceptLanguageItems()
        .firstNotNullOfOrNull { headerValue ->
            Localization.Language.entries.firstOrNull { language ->
                language.identifier.equals(headerValue.value, ignoreCase = true)
            }
        }
    return Localization(
        currentLanguage = languageFromHlQueryParameter
            ?: languageFromAcceptLanguageHeader
            ?: Localization.Language.EN
    )
}

fun PipelineContext<Unit, ApplicationCall>.hlParameterIfPresentAndValidLanguage(): String? {
    val languageFromHlQueryParameter = call.request.queryParameters["hl"]
        ?.takeIf { it.isNotBlank() }
        ?.let { hl ->
            Localization.Language.entries.firstOrNull { language ->
                language.identifier.equals(hl, ignoreCase = true)
            }
        }

    return languageFromHlQueryParameter?.identifier
}


fun Application.configure(
    pathPrefix: String?,
    keepFilesTimeInHours: Int,
    showGithubLinkInFooter: Boolean,
    sitePrivacyPolicyLink: String?,
    showLanguageSelectionInFooter: Boolean,
    shynet: Shynet,
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
            try {
                call.respondHtmlTemplate(
                    IndexTemplate(
                        pathPrefix = pathPrefix,
                        showGithubLinkInFooter = showGithubLinkInFooter,
                        sitePrivacyPolicyLink = sitePrivacyPolicyLink,
                        showLanguageSelectionInFooter = showLanguageSelectionInFooter,
                        keepFilesTimeInHours = keepFilesTimeInHours,
                        localization = localization(),
                        shynet = shynet,
                        hl = hlParameterIfPresentAndValidLanguage(),
                    )
                ) {

                }
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
                throw throwable
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
                        sitePrivacyPolicyLink = sitePrivacyPolicyLink,
                        showLanguageSelectionInFooter = showLanguageSelectionInFooter,
                        localization = localization(),
                        shynet = shynet,
                        hl = hlParameterIfPresentAndValidLanguage(),
                        dropId = dropId,
                    )
                ) {

                }
            }
        }

        get("site.webmanifest") {
            call.respondText(contentType = ContentType.parse("application/manifest+json")) {
                val localization = localization()
                """{"name":"${localization["html_site_title"]}","short_name":"${localization["html_site_title_short"]}","icons":[{"src":"/icon/android-chrome-192x192.png","sizes":"192x192","type":"image/png"},{"src":"/icon/android-chrome-512x512.png","sizes":"512x512","type":"image/png"}],"theme_color":"#fb8c00","background_color":"#ffffff","display":"standalone"}"""
            }
        }

        route("static") {
            get("frontend.js") {
                val localization = localization()
                val content = this.javaClass
                    .getResourceAsStream("/frontend.js")!!
                    .use { stream -> stream.reader().readText() }
                call.respondText(
                    contentType = ContentType.Text.JavaScript,
                    text = content
                        .replace("%%toast_clipboard_copy_success%%", localization["toast_clipboard_copy_success"])
                        .replace("%%toast_clipboard_copy_error%%", localization["toast_clipboard_copy_error"])
                        .replace("%%toast_please_enter_password%%", localization["toast_please_enter_password"])
                )
            }
//            resource(remotePath = "frontend.js", resource = "frontend.js")
            resource(remotePath = "drop.js", resource = "drop.js")
            resource(remotePath = "sjcl.js", resource = "sjcl/sjcl.js")

            resource(remotePath = "materialize.min.css", resource = "materialize/materialize.min.css")
            resource(remotePath = "materialize.min.js", resource = "materialize/materialize.min.js")
            resource(remotePath = "MaterialIcons.css", resource = "materialize/MaterialIcons.css")
            resource(remotePath = "MaterialIcons-Regular.ttf", resource = "materialize/MaterialIcons-Regular.ttf")
            resource(remotePath = "styles.css", resource = "styles.css")
        }

        staticResources(remotePath = "icon", basePackage = "icon")

    }
}
