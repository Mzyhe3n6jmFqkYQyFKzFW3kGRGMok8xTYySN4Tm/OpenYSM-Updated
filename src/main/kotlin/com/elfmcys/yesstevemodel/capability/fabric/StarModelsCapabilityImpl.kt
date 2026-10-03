package com.elfmcys.yesstevemodel.capability.fabric

import com.elfmcys.yesstevemodel.capability.StarModelsCapability
import com.elfmcys.yesstevemodel.fabric.YsmComponents
import net.minecraft.world.entity.player.Player

object StarModelsCapabilityImpl {
    @JvmStatic
    operator fun get(player: Player): StarModelsCapability? {
        val component = YsmComponents.STAR_MODELS.getNullable(player)
        return component?.capability
    }
}
