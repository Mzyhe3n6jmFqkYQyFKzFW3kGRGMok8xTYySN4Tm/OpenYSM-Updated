@file:Suppress("unused")

package com.elfmcys.yesstevemodel.util

import java.security.MessageDigest
import java.util.*

object DigestUtil {
    private val MD5_TL: ThreadLocal<MessageDigest> = ThreadLocal.withInitial {
        runCatching {
            MessageDigest.getInstance("MD5")
        }.getOrElse { e ->
            throw RuntimeException("MD5 algorithm not available", e)
        }
    }

    private val SHA256_TL: ThreadLocal<MessageDigest> = ThreadLocal.withInitial {
        runCatching {
            MessageDigest.getInstance("SHA-256")
        }.getOrElse { e ->
            throw RuntimeException("SHA-256 algorithm not available", e)
        }
    }

    fun md5Digest(): MessageDigest {
        val md = MD5_TL.get()
        md.reset()
        return md
    }

    fun sha256Digest(): MessageDigest {
        val md = SHA256_TL.get()
        md.reset()
        return md
    }

    fun md5(input: ByteArray): ByteArray {
        val md = MD5_TL.get()
        md.reset()
        return md.digest(input)
    }

    fun sha256(input: ByteArray): ByteArray {
        val md = SHA256_TL.get()
        md.reset()
        return md.digest(input)
    }

    fun md5Hex(input: ByteArray): String {
        return HexFormat.of().formatHex(md5(input))
    }

    fun sha256Hex(input: ByteArray): String {
        return HexFormat.of().formatHex(sha256(input))
    }
}