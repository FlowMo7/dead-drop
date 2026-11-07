package dev.moetz.deaddrop.data

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.util.*
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

class DataRepository(
    dataFolderPath: String,
    private val encryptionManager: EncryptionManager,
    private val keepFilesTime: Duration,
    private val timePeriodToSweepOverdueFiles: Duration,
    scope: CoroutineScope,
) {

    private val dataFolder: File = File(dataFolderPath)

    private val dataFolderCreateMutex = Mutex()

    init {
        if (dataFolder.exists().not()) {
            dataFolder.mkdirs()
            if (dataFolder.exists().not()) {
                throw IllegalStateException("Could not create data directory at '${dataFolder.absolutePath}'.")
            }
        }
        if (dataFolder.isDirectory.not()) {
            throw IllegalStateException("Data directory (${dataFolder.absolutePath}) already exists, but is not a directory.")
        }

        scope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(timePeriodToSweepOverdueFiles)
                cleanUpOverdueFiles()
            }
        }
    }

    private suspend fun cleanUpOverdueFiles() {
        withContext(Dispatchers.IO) {
            val cutOffDateTime = Clock.System.now().minus(keepFilesTime)
            dataFolder.listFiles()
                ?.toList()
                .orEmpty()
                .filter { file -> file.lastModifiedAsInstant() < cutOffDateTime }
                .forEach { file ->
                    val id = file.name
                    println("[DataRepository] Cleaning up drop with id $id")
                    file.delete()
                }
        }
    }

    private suspend fun getNewDropFile(): File {
        return dataFolderCreateMutex.withLock {
            withContext(Dispatchers.IO) {
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
    }

    suspend fun addDrop(content: String): String {
        val encryptedContent = encryptionManager.encrypt(content.toByteArray(Charsets.UTF_8))
        val file = getNewDropFile()
        val id = file.name
        withContext(Dispatchers.IO) {
            file.writeText(encryptedContent)
        }
        println("[DataRepository] Add drop with id $id")
        return id
    }

    private suspend fun File.isOverdue(): Boolean {
        val cutOffDateTime = Clock.System.now().minus(keepFilesTime)
        return this.lastModifiedAsInstant() < cutOffDateTime
    }

    suspend fun getDrop(id: String): String? {
        val file = File(dataFolder, id)
        return withContext(Dispatchers.IO) {
            val payload = if (file.exists()) {
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
            if (payload != null) {
                println("[DataRepository] Retrieving drop with id $id")
            } else {
                println("[DataRepository] Could not find drop with id $id")
            }
            payload
        }
    }

    private suspend fun File.lastModifiedAsInstant(): Instant {
        return withContext(Dispatchers.IO) {
            val timestamp = this@lastModifiedAsInstant.lastModified()
            Instant.fromEpochMilliseconds(timestamp)
        }
    }

}
