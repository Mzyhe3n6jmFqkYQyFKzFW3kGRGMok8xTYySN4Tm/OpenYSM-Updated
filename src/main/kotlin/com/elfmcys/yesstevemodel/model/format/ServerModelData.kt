@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.elfmcys.yesstevemodel.model.format

import com.elfmcys.yesstevemodel.util.FileTypeUtil
import net.minecraft.resources.Identifier

class ServerModelData(
    val modelId: String,
    val serverAnimationInfo: ServerAnimationInfo,
    private var projectiles: List<Array<String>>?,
    private var vehicles: List<Array<String>>?,
    private val info: ServerModelInfo,
    // TODO: Custom skin support
    private val isCustomSkinModel: Boolean,
    private val isAuth: Boolean
) {
    private val entityTypes: MutableSet<Identifier> = HashSet()
    private val excludedEntityTypes: MutableSet<Identifier> = HashSet()

    val modelInfo: ServerAnimationInfo = serverAnimationInfo

    fun getEntityTypes(): Set<Identifier> {
        projectiles?.let { list ->
            for (arr in list) {
                entityTypes.addAll(FileTypeUtil.resolveEntityTypes(arr))
            }
            projectiles = null
        }
        return entityTypes
    }

    fun getExcludedEntityTypes(): Set<Identifier> {
        vehicles?.let { list ->
            for (arr in list) {
                excludedEntityTypes.addAll(FileTypeUtil.resolveEntityTypes(arr))
            }
            vehicles = null
        }
        return excludedEntityTypes
    }

    fun getLoadedModelData(): ServerModelInfo = info

    fun isCustomSkinModel(): Boolean = isCustomSkinModel

    fun isAuth(): Boolean = isAuth
}
