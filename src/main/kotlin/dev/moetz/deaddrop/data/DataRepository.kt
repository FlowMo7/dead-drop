@file:OptIn(ExperimentalTime::class)

package dev.moetz.deaddrop.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class DataRepository(
    private val dataFolderPath: String,
    private val encryptionManager: EncryptionManager,
    private val keepFilesTime: Duration,
    private val timePeriodToSweepOverdueFiles: Duration,
) {

    private val dataFolder: File
        get() {
            return File(dataFolderPath).also { folder ->
                if (folder.exists().not()) {
                    folder.mkdirs()
                    if (folder.exists().not()) {
                        throw IllegalStateException("Could not create data directory at ${folder.absolutePath}")
                    }
                }
                if (folder.isDirectory.not()) {
                    throw IllegalStateException("data directory (${folder.absolutePath}) already exists, but is not a directory")
                }
            }
        }

    private val dataFolderCreateMutex = Mutex()

    init {
        GlobalScope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(timePeriodToSweepOverdueFiles)
                cleanUpOverdueFiles()
            }
        }
    }

    private suspend fun cleanUpOverdueFiles() {
        withContext(Dispatchers.IO) {
            val cutOffDateTime = Clock.System.now().minus(keepFilesTime)
            dataFolder.listFiles()?.toList()
                .orEmpty()
                .filter { file -> file.lastModifiedAsInstant() < cutOffDateTime }
                .forEach { file -> file.delete() }
        }
    }

    private suspend fun getNewDropFile(): File {
        return dataFolderCreateMutex.withLock {
            var fileName: String
            do {
                fileName = HashUtils.sha256(UUID.randomUUID().toString()).substring(0 until 12)
            } while (File(dataFolder, fileName).exists())
            File(dataFolder, fileName).also {
                if (it.createNewFile().not()) {
                    throw IllegalStateException("Could not create a new file for drop (${it.absolutePath})")
                }
            }
        }
    }

    suspend fun addDrop(content: String): String {
        val encryptedContent = encryptionManager.encrypt(content.toByteArray(Charsets.UTF_8))
        val file = getNewDropFile()
        file.writeText(encryptedContent)
        return file.name
    }

    private fun File.isOverdue(): Boolean {
        val cutOffDateTime = Clock.System.now().minus(keepFilesTime)
        return this.lastModifiedAsInstant() < cutOffDateTime
    }

    suspend fun getDrop(id: String): String? {
        val file = File(dataFolder, id)
        return if (file.exists()) {
            if (file.isOverdue().not()) {
                //valid timeframe
                val encryptedContent = file.readText()
                file.delete()
                val decryptedByteArray = encryptionManager.decrypt(encryptedContent)
                decryptedByteArray.toString(Charsets.UTF_8)
            } else {
                //invalid timeframe, delete
                file.delete()
                null
            }
        } else {
            null
        }
    }

    private fun File.lastModifiedAsInstant(): Instant {
        val timestamp = this.lastModified()
        return Instant.fromEpochMilliseconds(timestamp)
    }

}
