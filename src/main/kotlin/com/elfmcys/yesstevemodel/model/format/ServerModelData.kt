@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.elfmcys.yesstevemodel.model.format

import com.elfmcys.yesstevemodel.util.FileTypeUtil
import net.minecraft.resources.Identifier

class ServerModelData(
    val modelId: String,
    val serverAnimationInfo: ServerAnimationInfo,
    private var projectiles: Array<Any>?,
    private var vehicles: Array<Any>?,
    private val info: ServerModelInfo,
    private val isCustomSkinModel: Boolean,
    private val isAuth: Boolean
) {
    private val entityTypes: MutableSet<Identifier> = HashSet()
    private val excludedEntityTypes: MutableSet<Identifier> = HashSet()

    val modelInfo: ServerAnimationInfo = serverAnimationInfo

    fun getEntityTypes(): Set<Identifier> {
        projectiles?.let { array ->
            for (obj in array) {
                if (obj is Array<*>) {
                    @Suppress("UNCHECKED_CAST")
                    entityTypes.addAll(FileTypeUtil.resolveEntityTypes(obj as Array<String>))
                }
            }
            projectiles = null
        }
        return entityTypes
    }

    fun getExcludedEntityTypes(): Set<Identifier> {
        vehicles?.let { array ->
            for (obj in array) {
                if (obj is Array<*>) {
                    @Suppress("UNCHECKED_CAST")
                    excludedEntityTypes.addAll(FileTypeUtil.resolveEntityTypes(obj as Array<String>))
                }
            }
            vehicles = null
        }
        return excludedEntityTypes
    }

    fun getLoadedModelData(): ServerModelInfo = info

    fun isCustomSkinModel(): Boolean = isCustomSkinModel

    fun isAuth(): Boolean = isAuth
}
