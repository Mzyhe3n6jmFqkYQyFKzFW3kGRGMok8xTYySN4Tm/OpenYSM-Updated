package com.elfmcys.yesstevemodel.fabric

import com.elfmcys.yesstevemodel.NameSpaces
import com.elfmcys.yesstevemodel.capability.fabric.ModelInfoComponent
import com.elfmcys.yesstevemodel.capability.fabric.ProjectileModelComponent
import com.elfmcys.yesstevemodel.capability.fabric.VehicleModelComponent
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.projectile.Projectile
import org.ladysnake.cca.api.v3.component.ComponentKey
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy

class YsmComponents : EntityComponentInitializer {
    companion object {
        val MODEL_INFO: ComponentKey<ModelInfoComponent> =
            ComponentRegistryV3.INSTANCE.getOrCreate(NameSpaces.MOD.path("model_info"), ModelInfoComponent::class.java)

        val PROJECTILE_MODEL: ComponentKey<ProjectileModelComponent> = ComponentRegistryV3.INSTANCE.getOrCreate(
            NameSpaces.MOD.path("projectile_model"),
            ProjectileModelComponent::class.java
        )

        val VEHICLE_MODEL: ComponentKey<VehicleModelComponent> = ComponentRegistryV3.INSTANCE.getOrCreate(
            NameSpaces.MOD.path("vehicle_model"),
            VehicleModelComponent::class.java
        )
    }

    override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
        registry.registerForPlayers(MODEL_INFO, { ModelInfoComponent() }, RespawnCopyStrategy.ALWAYS_COPY)
        registry.registerFor(Projectile::class.java, PROJECTILE_MODEL) { ProjectileModelComponent() }
        registry.registerFor(Entity::class.java, VEHICLE_MODEL) { VehicleModelComponent() }
    }
}
