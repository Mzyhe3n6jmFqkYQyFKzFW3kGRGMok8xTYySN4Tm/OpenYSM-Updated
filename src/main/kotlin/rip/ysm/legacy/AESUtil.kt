package rip.ysm.legacy

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.SecureRandom
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AESUtil {
    @JvmStatic
    @Throws(IOException::class, GeneralSecurityException::class)
    fun encrypt(key: SecretKey, iv: AlgorithmParameterSpec, input: ByteArray): ByteArrayOutputStream {
        val inputStream = ByteArrayInputStream(input)
        val outputStream = ByteArrayOutputStream()
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)
        val buffer = ByteArray(64)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            val output = cipher.update(buffer, 0, bytesRead)
            if (output != null) {
                outputStream.write(output)
            }
        }
        val outputBytes = cipher.doFinal()
        if (outputBytes != null) {
            outputStream.write(outputBytes)
        }
        return outputStream
    }

    @JvmStatic
    @Throws(IOException::class, GeneralSecurityException::class)
    fun decrypt(key: SecretKey, iv: AlgorithmParameterSpec, input: ByteArray): ByteArrayOutputStream {
        val inputStream = ByteArrayInputStream(input)
        val outputStream = ByteArrayOutputStream()
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key, iv)
        val buffer = ByteArray(64)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            val output = cipher.update(buffer, 0, bytesRead)
            if (output != null) {
                outputStream.write(output)
            }
        }
        val outputBytes = cipher.doFinal()
        if (outputBytes != null) {
            outputStream.write(outputBytes)
        }
        return outputStream
    }

    @JvmStatic
    fun generateKey(): SecretKey {
        val generator = runCatching {
            KeyGenerator.getInstance("AES")
        }.getOrElse { e ->
            throw RuntimeException(e)
        }
        generator.init(128)
        return generator.generateKey()
    }

    @JvmStatic
    fun getKey(bytes: ByteArray): SecretKey {
        return SecretKeySpec(bytes, "AES")
    }

    @JvmStatic
    fun generateIv(): IvParameterSpec {
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        return IvParameterSpec(iv)
    }
}