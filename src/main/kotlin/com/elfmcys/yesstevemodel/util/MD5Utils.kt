package com.elfmcys.yesstevemodel.util

import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

object MD5Utils {
    @JvmStatic
    fun getStreamMD5(inputStream: InputStream): String {
        return try {
            DigestUtils.md5Hex(inputStream)
        } catch (e: IOException) {
            ""
        }
    }

    @JvmStatic
    fun getFileMD5(file: File): String {
        if (!file.exists()) {
            return ""
        }
        return try {
            FileInputStream(file).use { getStreamMD5(it) }
        } catch (e: IOException) {
            ""
        }
    }

    @JvmStatic
    fun getFileMD5(filepath: String): String {
        return getFileMD5(File(filepath))
    }
}
