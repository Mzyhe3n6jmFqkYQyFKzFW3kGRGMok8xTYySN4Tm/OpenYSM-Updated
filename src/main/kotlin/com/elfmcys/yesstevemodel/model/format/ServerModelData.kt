@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.elfmcys.yesstevemodel.model.format

import com.elfmcys.yesstevemodel.util.FileTypeUtil
import net.minecraft.resources.Identifier

data class ServerModelData(
    val modelId: String,
    private val serverAnimationInfo: ServerAnimationInfo,
    private var projectiles: List<Array<String>>?,
    private var vehicles: List<Array<String>>?,
    private val info: ServerModelInfo,
    val isCustomSkinModel: Boolean,
    val isAuth: Boolean
) {
    private val _entityTypes: MutableSet<Identifier> = HashSet()
    private val _excludedEntityTypes: MutableSet<Identifier> = HashSet()

    val modelInfo: ServerAnimationInfo = serverAnimationInfo

    val entityTypes: Set<Identifier>
        get() {
            projectiles?.let { list ->
                for (arr in list) {
                    _entityTypes.addAll(FileTypeUtil.resolveEntityTypes(arr))
                }
                projectiles = null
            }
            return _entityTypes
        }

    val excludedEntityTypes: Set<Identifier>
        get() {
            vehicles?.let { list ->
                for (arr in list) {
                    _excludedEntityTypes.addAll(FileTypeUtil.resolveEntityTypes(arr))
                }
                vehicles = null
            }
            return _excludedEntityTypes
        }

    val loadedModelData: ServerModelInfo
        get() = info

    val useMcDefaultTexture: Int
        get() = info.modelProperties.useMcDefaultTexture
}
