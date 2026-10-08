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
    // TODO: Custom skin support
    val isCustomSkinModel: Boolean,
    val isAuth: Boolean
) {
    private val entityTypes2: MutableSet<Identifier> = HashSet()
    private val excludedEntityTypes2: MutableSet<Identifier> = HashSet()

    val modelInfo: ServerAnimationInfo = serverAnimationInfo

    val entityTypes: Set<Identifier>
        get() {
            projectiles?.let { list ->
                for (arr in list) {
                    entityTypes2.addAll(FileTypeUtil.resolveEntityTypes(arr))
                }
                projectiles = null
            }
            return entityTypes2
        }

    val excludedEntityTypes: Set<Identifier>
        get() {
            vehicles?.let { list ->
                for (arr in list) {
                    excludedEntityTypes2.addAll(FileTypeUtil.resolveEntityTypes(arr))
                }
                vehicles = null
            }
            return excludedEntityTypes2
        }

    val loadedModelData: ServerModelInfo
        get() = info
}
