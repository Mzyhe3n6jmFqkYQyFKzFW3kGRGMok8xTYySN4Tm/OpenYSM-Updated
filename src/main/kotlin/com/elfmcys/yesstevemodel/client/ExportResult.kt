package com.elfmcys.yesstevemodel.client

import net.minecraft.network.chat.Component

class ExportResult(
    val success: Boolean,
    val message: Component?,
    val filePath: String,
    val fileName: String,
    val fileSize: Int
) {
    fun isSuccess(): Boolean = success
    fun getMessage(): Component? = message
    fun getFilePath(): String = filePath
    fun getFileName(): String = fileName
    fun getFileSize(): Int = fileSize
}