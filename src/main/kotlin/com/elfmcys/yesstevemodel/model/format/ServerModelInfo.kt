@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.elfmcys.yesstevemodel.model.format

import com.elfmcys.yesstevemodel.resource.models.MainModelInfo
import com.elfmcys.yesstevemodel.resource.models.Metadata
import com.elfmcys.yesstevemodel.resource.models.ModelProperties
import com.elfmcys.yesstevemodel.util.FileTypeUtil

data class ServerModelInfo(
    val metadata: Metadata?,
    val modelProperties: ModelProperties,
    val mainModelInfo: MainModelInfo,
    val formatVersion: Int,
    val modelHash: String,
    val extra: String,
    val timestamp: Long,
    val rand: String
) {
    val hashId: Int = FileTypeUtil.parseHexId(modelHash)
}
