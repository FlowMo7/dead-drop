package dev.moetz.deaddrop.data

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

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

class EncryptionManager(
    private val secretKeyFile: File
) {

    private val secretKeyFileLock = Any()
    private fun getOrCreateSecretKey(): SecretKey {
        return synchronized(secretKeyFileLock) {
            if (secretKeyFile.exists()) {
                //TODO error handling when key cannot be read
                loadSecretKeyFromFile()
            } else {
                val key = createSecretKey()
                val encodedKey = key.encoded.base64Encode()
                if (secretKeyFile.parentFile.exists().not()) {
                    secretKeyFile.parentFile.mkdirs()
                    if (secretKeyFile.parentFile.exists().not()) {
                        throw IllegalStateException("Could not create directory ${secretKeyFile.parentFile.absolutePath}")
                    }
                }
                secretKeyFile.createNewFile()
                if (secretKeyFile.exists().not()) {
                    throw IllegalStateException("Could not create file at ${secretKeyFile.absolutePath}")
                }
                secretKeyFile.writeText(encodedKey)
                loadSecretKeyFromFile()
            }
        }
    }

    private fun loadSecretKeyFromFile(): SecretKey {
        val decodedKeyByteArray = secretKeyFile.readText().base64Decode()
        return SecretKeySpec(decodedKeyByteArray, 0, decodedKeyByteArray.size, "AES")
    }

    private fun createSecretKey(): SecretKey {
        val secureRandom = SecureRandom()
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256, secureRandom)
        return keyGenerator.generateKey()
    }

    private fun getCipher(): Cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")

    fun encrypt(byteArray: ByteArray): String {
        val secretKey = getOrCreateSecretKey()

        val cipher = getCipher()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val cipherText = cipher.doFinal(byteArray)

        val initalizationVector = cipher.iv

        return initalizationVector.base64Encode() + "\n" + cipherText.base64Encode()
    }

    fun decrypt(cipherText: String): ByteArray {
        val secretKey = getOrCreateSecretKey()

        val (initializationVectorPart, cipherTextPart) = cipherText.split("\n", limit = 2)

        val cipher = getCipher()
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(initializationVectorPart.base64Decode()))

        val decrypted = cipher.doFinal(cipherTextPart.base64Decode())
        return decrypted
    }

//    private fun getSecretKey(): SecretKey {
//        val decodedKeyByteArray = SECRET_KEY_BASE_64.base64Decode()
//        return SecretKeySpec(decodedKeyByteArray, 0, decodedKeyByteArray.size, "AES")
//    }

    private fun ByteArray.base64Encode(): String {
        return Base64.getEncoder().encodeToString(this)
    }

    private fun String.base64Decode(): ByteArray {
        return Base64.getDecoder().decode(this.toByteArray(Charsets.UTF_8))
    }

}

private object HashUtils {
//    fun sha512(input: String) = hashString("SHA-512", input)

    fun sha256(input: String) = hashString("SHA-256", input)

//    fun sha1(input: String) = hashString("SHA-1", input)

    /**
     * Supported algorithms on Android:
     *
     * Algorithm	Supported API Levels
     * MD5          1+
     * SHA-1	    1+
     * SHA-224	    1-8,22+
     * SHA-256	    1+
     * SHA-384	    1+
     * SHA-512	    1+
     */
    private fun hashString(type: String, input: String): String {
        val HEX_CHARS = "0123456789ABCDEF"
        val bytes = MessageDigest
            .getInstance(type)
            .digest(input.toByteArray())
        val result = StringBuilder(bytes.size * 2)

        bytes.forEach {
            val i = it.toInt()
            result.append(HEX_CHARS[i shr 4 and 0x0f])
            result.append(HEX_CHARS[i and 0x0f])
        }

        return result.toString()
    }
}