package dev.moetz.deaddrop.data

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class DataRepository(
    private val dataFolderPath: String,
    private val encryptionManager: EncryptionManager,
    private val keepFilesTimeInSeconds: Long,
    private val timePeriodToSweepOverdueFilesInSeconds: Long
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
        GlobalScope.launch {
            while (isActive) {
                delay(1000 * timePeriodToSweepOverdueFilesInSeconds)
                cleanUpOverdueFiles()
            }
        }
    }

    private suspend fun cleanUpOverdueFiles() {
        val cutOffDateTime = LocalDateTime.now().minusSeconds(keepFilesTimeInSeconds)
        dataFolder.listFiles()?.toList()
            .orEmpty()
            .filter { file -> file.lastModifiedAsLocalDateTime().isBefore(cutOffDateTime) }
            .forEach { file -> file.delete() }
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
        val cutOffDateTime = LocalDateTime.now().minusSeconds(keepFilesTimeInSeconds)
        return this.lastModifiedAsLocalDateTime().isBefore(cutOffDateTime)
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

    private fun File.lastModifiedAsLocalDateTime(): LocalDateTime {
        val timestamp = this.lastModified()
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
    }

}
