package dev.moetz.deaddrop.plugins

import dev.moetz.deaddrop.Localization
import dev.moetz.deaddrop.Shynet
import dev.moetz.deaddrop.combinePartsToFullUrl
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
    isHttps: Boolean,
    domain: String,
) {

    routing {

        get("robots.txt") {
            call.respondText(ContentType.Text.Plain) {
                "User-agent: * Allow: /"
            }
        }

        get("llms.txt") {
            val baseUrl = combinePartsToFullUrl(isHttps, domain, pathPrefix)
            call.respondText(ContentType.Text.Plain) {
                buildLlmsTxt(baseUrl)
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
                        .replaceWithLocalizedEntryAndEscapeSingleQuotes(
                            localization = localization,
                            placeholder = "%%toast_send_drop_message_empty%%",
                            localizedKey = "toast_send_drop_message_empty",
                        )
                        .replaceWithLocalizedEntryAndEscapeSingleQuotes(
                            localization = localization,
                            placeholder = "%%toast_clipboard_copy_error%%",
                            localizedKey = "toast_clipboard_copy_error",
                        )
                        .replaceWithLocalizedEntryAndEscapeSingleQuotes(
                            localization = localization,
                            placeholder = "%%toast_please_enter_password%%",
                            localizedKey = "toast_please_enter_password",
                        )
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

        staticResources(remotePath = "flags", basePackage = "flags")

    }
}

private fun String.replaceWithLocalizedEntryAndEscapeSingleQuotes(
    localization: Localization,
    placeholder: String,
    localizedKey: String
): String {
    return this.replace(
        placeholder,
        localization[localizedKey].replace("\'", "\\\'")
    )
}

private fun buildLlmsTxt(baseUrl: String): String {
    return """# Dead Drop Service

> Send one-time secrets securely. Data is encrypted in the browser with a
> password that is never sent to the server; the server stores only the
> encrypted blob and hands out a single-use pickup link.

Dead Drop is a self-hostable one-time-secret sharing service built with
Kotlin and Ktor. Secrets are encrypted client-side using sjcl, encrypted
again at rest on the server, retrievable exactly once, and automatically
deleted after a configurable expiry. Licensed under Apache-2.0.

## Pages

- [Home / create a drop](${baseUrl}): Web UI to encrypt a secret and generate a one-time link
- [Pickup a drop](${baseUrl}pickup/{id}): One-time retrieval page for a given drop id
- [Service status](${baseUrl}status): Plain-text health check endpoint

## HTTP API

- [Create drop](${baseUrl}api/drop): POST the client-encrypted payload as the request body; returns JSON {"id","pickupUrl"}
- [Retrieve drop](${baseUrl}api/drop/{id}): GET the encrypted payload once; the drop is deleted on retrieval and returns 404 afterwards or when expired

## Optional

- [Web app manifest](${baseUrl}site.webmanifest): PWA manifest
- [robots.txt](${baseUrl}robots.txt): Crawl policy
""".trimIndent()
}
