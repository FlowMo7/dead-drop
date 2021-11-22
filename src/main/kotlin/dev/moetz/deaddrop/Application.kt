package dev.moetz.deaddrop

import dev.moetz.deaddrop.data.DataRepository
import dev.moetz.deaddrop.data.EncryptionManager
import dev.moetz.deaddrop.plugins.configure
import dev.moetz.deaddrop.plugins.configureApi
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.io.File

fun main() {

    val domain = System.getenv("DOMAIN")?.takeIf { it.isNotBlank() } ?: "localhost:8080"
    val isHttps = System.getenv("IS_HTTPS")?.takeIf { it.isNotBlank() }?.toBoolean() ?: false
    val dataDirectory = System.getenv("DATA_DIRECTORY")?.takeIf { it.isNotBlank() } ?: "./data"
    val encryptionKeyPath = System.getenv("ENCRYPTION_KEY_PATH")?.takeIf { it.isNotBlank() } ?: "./config/key.secret"

    val keepFilesTimeInHours = System.getenv("FILE_KEEP_TIME_IN_HOURS")
        ?.takeIf { it.isNotBlank() }
        ?.toIntOrNull()
        ?: 24/* 24 hours */

    val encryptionManager = EncryptionManager(File(encryptionKeyPath))
    val dataRepository = DataRepository(
        dataFolderPath = dataDirectory,
        encryptionManager = encryptionManager,
        keepFilesTimeInSeconds = (60L * 60 * keepFilesTimeInHours),
        timePeriodToSweepOverdueFilesInSeconds = (60L * 60) /* every hour */
    )

    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(DefaultHeaders)
        install(AutoHeadResponse)
        install(XForwardedHeaderSupport)

        install(Compression) {
            gzip()
            deflate()
        }
        configure(domain, isHttps, keepFilesTimeInHours)
        configureApi(dataRepository, isHttps, domain)
    }.start(wait = true)
}
