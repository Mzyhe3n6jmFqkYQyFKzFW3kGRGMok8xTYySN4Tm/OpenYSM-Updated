package com.elfmcys.yesstevemodel.capability.fabric

import com.elfmcys.yesstevemodel.capability.ModelInfoCapability
import com.elfmcys.yesstevemodel.fabric.YsmComponents
import net.minecraft.world.entity.player.Player

object ModelInfoCapabilityImpl {
    operator fun get(player: Player): ModelInfoCapability? {
        val component = YsmComponents.MODEL_INFO.getNullable(player)
        return component?.capability
    }
}
