package com.elfmcys.yesstevemodel.capability.fabric

import com.elfmcys.yesstevemodel.capability.AuthModelsCapability
import com.elfmcys.yesstevemodel.fabric.YsmComponents
import net.minecraft.world.entity.player.Player

object AuthModelsCapabilityImpl {
    @JvmStatic
    operator fun get(player: Player): AuthModelsCapability? {
        val component = YsmComponents.AUTH_MODELS.getNullable(player)
        return component?.capability
    }
}
