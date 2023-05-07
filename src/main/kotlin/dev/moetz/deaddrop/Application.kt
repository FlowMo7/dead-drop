package dev.moetz.deaddrop

import dev.moetz.deaddrop.data.DataRepository
import dev.moetz.deaddrop.data.EncryptionManager
import dev.moetz.deaddrop.plugins.configure
import dev.moetz.deaddrop.plugins.configureApi
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.conditionalheaders.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.forwardedheaders.*
import java.io.File

fun main() {

    val port = System.getenv("PORT")?.takeIf { it.isNotBlank() } ?: "8080"
    val domain = System.getenv("DOMAIN")?.takeIf { it.isNotBlank() } ?: "localhost:$port"
    val pathPrefix = System.getenv("PATH_PREFIX")?.takeIf { it.isNotBlank() }
    val isHttps = System.getenv("IS_HTTPS")?.takeIf { it.isNotBlank() }?.toBoolean() ?: true
    val dataDirectory = "/var/dead-drop/data" // "./data" //for development purpose
    val encryptionKeyPath = "/var/dead-drop/key/key.secret"// "./config/key.secret" //for development purpose
    val siteTitle = System.getenv("SITE_TITLE")?.takeIf { it.isNotBlank() } ?: "Dead-Drop: Send secure information"
    val siteTitleShort = System.getenv("SITE_TITLE_SHORT")?.takeIf { it.isNotBlank() } ?: "Dead-Drop"

    //color code customization currently only works with colors where white text is visible on.
    val colorCode = System.getenv("COLOR_CODE")
        ?.takeIf { it.isNotBlank() }
        ?.let { colorCode -> colorCode.filter { it.isDigit() || it.lowercaseChar() in 'a'..'f' } }
        ?.takeIf { it.length == 3 || it.length == 6 }
        ?: "ff9800"

    val keepFilesTimeInHours = System.getenv("FILE_KEEP_TIME_IN_HOURS")
        ?.takeIf { it.isNotBlank() }
        ?.toIntOrNull()
        ?: 24

    val showGithubLinkInFooter =
        System.getenv("SHOW_GITHUB_LINK_IN_FOOTER")?.takeIf { it.isNotBlank() }?.toBoolean() ?: true

    val encryptionManager = EncryptionManager(File(encryptionKeyPath))
    val dataRepository = DataRepository(
        dataFolderPath = dataDirectory,
        encryptionManager = encryptionManager,
        keepFilesTimeInSeconds = (60L * 60 * keepFilesTimeInHours),
        timePeriodToSweepOverdueFilesInSeconds = (60L * 60) /* every hour */
    )

    embeddedServer(Netty, port = port.toInt(), host = "0.0.0.0") {
        install(DefaultHeaders)
        install(AutoHeadResponse)
        install(XForwardedHeaders)
        install(CachingHeaders) {
            options { _, outgoingContent ->
                when (outgoingContent.contentType?.withoutParameters()) {
                    ContentType.Text.CSS,
                    ContentType.Text.JavaScript,
                    ContentType.Application.JavaScript,
                    ContentType("application", "manifest+json"),
                    ContentType.Image.PNG -> {
                        CachingOptions(
                            CacheControl.MaxAge(
                                maxAgeSeconds = 24 * 60 * 60,    //24 hours
                                visibility = CacheControl.Visibility.Public
                            )
                        )
                    }

                    ContentType.Application.Json,
                    ContentType.Text.Plain -> {
                        CachingOptions(CacheControl.NoCache(null))
                    }

                    else -> null
                }
            }
        }
        install(ConditionalHeaders)

        install(Compression) {
            gzip {
                priority = 1.0
            }
            deflate {
                priority = 10.0
                minimumSize(1024)
            }
        }

        configure(
            pathPrefix = pathPrefix,
            keepFilesTimeInHours = keepFilesTimeInHours,
            showGithubLinkInFooter = showGithubLinkInFooter,
            colorCode = colorCode,
            siteTitle = siteTitle,
            siteTitleShort = siteTitleShort
        )
        configureApi(dataRepository, isHttps, domain, pathPrefix)
    }.start(wait = true)
}
