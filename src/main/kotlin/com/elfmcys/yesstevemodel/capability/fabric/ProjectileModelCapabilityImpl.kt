package com.elfmcys.yesstevemodel.capability.fabric

import com.elfmcys.yesstevemodel.capability.ProjectileModelCapability
import com.elfmcys.yesstevemodel.fabric.YsmComponents
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.projectile.Projectile

object ProjectileModelCapabilityImpl {
    operator fun get(entity: Entity): ProjectileModelCapability? {
        if (entity !is Projectile) return null
        val component = YsmComponents.PROJECTILE_MODEL.getNullable(entity)
        return component?.capability
    }

    operator fun get(projectile: Projectile): ProjectileModelCapability? {
        val component = YsmComponents.PROJECTILE_MODEL.getNullable(projectile)
        return component?.capability
    }
}
