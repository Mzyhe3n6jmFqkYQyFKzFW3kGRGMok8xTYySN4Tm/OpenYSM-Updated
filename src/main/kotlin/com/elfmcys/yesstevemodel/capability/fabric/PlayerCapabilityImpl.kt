package com.elfmcys.yesstevemodel.capability.fabric

import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.capability.fabric.client.PlayerCapabilityClientStore
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player

object PlayerCapabilityImpl {
    @JvmStatic
    operator fun get(player: Player): PlayerCapability? {
        if (FabricLoader.getInstance().environmentType != EnvType.CLIENT) return null
        return PlayerCapabilityClientStore[player]
    }

    @JvmStatic
    operator fun get(entity: Entity): PlayerCapability? {
        if (entity !is Player) return null
        return get(entity)
    }
}
