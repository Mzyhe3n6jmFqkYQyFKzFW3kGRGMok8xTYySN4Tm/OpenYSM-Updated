package com.elfmcys.yesstevemodel.client

import net.minecraft.network.chat.Component

class ExportResult(
    val success: Boolean,
    val message: Component?,
    val filePath: String,
    val fileName: String,
    val fileSize: Int
)