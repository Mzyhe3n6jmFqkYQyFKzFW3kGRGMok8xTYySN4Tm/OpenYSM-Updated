@file:Suppress("unused")

package com.elfmcys.yesstevemodel.capability

import com.elfmcys.yesstevemodel.capability.fabric.StarModelsCapabilityImpl
import com.google.common.collect.Sets
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.player.Player

class StarModelsCapability {
    private var starModels: MutableSet<String> = Sets.newHashSet()

    fun getStarModels(): MutableSet<String> = starModels

    fun setStarModels(set: MutableSet<String>) {
        starModels = set
    }

    fun containsModel(str: String): Boolean = starModels.contains(str)

    fun addModel(str: String) = starModels.add(str)

    fun removeModel(str: String) = starModels.remove(str)

    fun clear() = starModels.clear()

    fun serializeNBT(): CompoundTag {
        val compoundTag = CompoundTag()
        val compoundTag2 = CompoundTag()
        for (str in starModels) {
            compoundTag2.putBoolean(str, true)
        }
        compoundTag.put("star_models", compoundTag2)
        return compoundTag
    }

    fun deserializeNBT(compoundTag: CompoundTag) {
        starModels.clear()
        val compound = compoundTag.getCompoundOrEmpty("star_models")
        for (str in compound.keySet()) {
            starModels.add(str)
        }
    }

    companion object {
        @JvmStatic
        operator fun get(player: Player): StarModelsCapability? = StarModelsCapabilityImpl[player]
    }
}
