package dev.moetz.deaddrop.data

import java.io.File
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

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