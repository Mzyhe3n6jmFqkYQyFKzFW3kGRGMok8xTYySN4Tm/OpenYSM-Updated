package com.elfmcys.yesstevemodel.resource.models

import com.elfmcys.yesstevemodel.client.texture.OuterFileTexture

/**
 * 读取ysm_pack.json内容
 * https://ysm.cfpa.team/wiki/model-pack/#%E5%88%B6%E4%BD%9C%E6%A8%A1%E5%9E%8B%E5%8C%85
 */
data class ModelPackData(
    val path: String,
    val name: String,
    val description: String?,
    val texture: OuterFileTexture?,
    val translations: Map<String, Map<String, String>>?
)
