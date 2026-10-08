@file:Suppress("unused")

package com.elfmcys.yesstevemodel.capability

import com.elfmcys.yesstevemodel.capability.fabric.AuthModelsCapabilityImpl
import com.google.common.collect.Sets
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.world.entity.player.Player

class AuthModelsCapability {
    var authModels: MutableSet<String> = Sets.newHashSet()

    fun addModel(str: String) = authModels.add(str)

    fun containsModel(str: String) = authModels.contains(str)

    fun removeModel(str: String) = authModels.remove(str)

    fun clear() = authModels.clear()

    fun serializeNBT(): ListTag {
        val listTag = ListTag()
        for (authModel in authModels) {
            listTag.add(StringTag.valueOf(authModel))
        }
        return listTag
    }

    fun deserializeNBT(listTag: ListTag) {
        authModels.clear()
        for (tag in listTag) {
            tag.asString().orElse("")?.let { authModels.add(it) }
        }
    }

    companion object {
        @JvmStatic
        operator fun get(player: Player): AuthModelsCapability? {
            return AuthModelsCapabilityImpl[player]
        }
    }
}
