@file:Suppress("unused")

package com.elfmcys.yesstevemodel.capability

import com.elfmcys.yesstevemodel.capability.fabric.StarModelsCapabilityImpl
import com.elfmcys.yesstevemodel.client.ClientOnlySelection
import com.google.common.collect.Sets
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.world.entity.player.Player

class StarModelsCapability {
    var starModels: MutableSet<String> = Sets.newConcurrentHashSet()

    init {
        runCatching {
            if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
                starModels.addAll(ClientOnlySelection.getStarModels())
            }
        }
    }

    fun containsModel(str: String): Boolean =
        starModels.contains(str) || FabricLoader.getInstance().environmentType == EnvType.CLIENT && ClientOnlySelection.isModelStarred(
            str
        )

    fun addModel(str: String): Boolean {
        val added = starModels.add(str)
        runCatching {
            if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
                ClientOnlySelection.addStarModel(str)
            }
        }
        return added
    }

    fun removeModel(str: String): Boolean {
        val removed = starModels.remove(str)
        runCatching {
            if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
                ClientOnlySelection.removeStarModel(str)
            }
        }
        return removed
    }

    fun clear() {
        starModels.clear()
        runCatching {
            if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
                for (model in ClientOnlySelection.getStarModels()) {
                    ClientOnlySelection.removeStarModel(model)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        operator fun get(player: Player): StarModelsCapability? = StarModelsCapabilityImpl[player]
    }
}
