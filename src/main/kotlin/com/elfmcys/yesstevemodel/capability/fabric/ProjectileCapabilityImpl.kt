package com.elfmcys.yesstevemodel.capability.fabric

import com.elfmcys.yesstevemodel.capability.ProjectileCapability
import com.elfmcys.yesstevemodel.capability.fabric.client.ProjectileCapabilityClientStore
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.projectile.Projectile

object ProjectileCapabilityImpl {
    @JvmStatic
    operator fun get(entity: Entity): ProjectileCapability? {
        if (entity !is Projectile) return null
        return get(entity)
    }

    @JvmStatic
    operator fun get(projectile: Projectile): ProjectileCapability? {
        if (FabricLoader.getInstance().environmentType != EnvType.CLIENT || !projectile.level().isClientSide) return null
        return ProjectileCapabilityClientStore[projectile]
    }
}
