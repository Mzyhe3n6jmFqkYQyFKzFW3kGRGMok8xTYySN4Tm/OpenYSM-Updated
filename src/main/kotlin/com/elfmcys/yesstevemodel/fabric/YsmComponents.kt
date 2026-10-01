package com.elfmcys.yesstevemodel.fabric

import com.elfmcys.yesstevemodel.NameSpaces
import com.elfmcys.yesstevemodel.capability.fabric.*
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.projectile.Projectile
import org.ladysnake.cca.api.v3.component.ComponentKey
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy

class YsmComponents : EntityComponentInitializer {
    companion object {
        @JvmField
        val STAR_MODELS: ComponentKey<StarModelsComponent> = ComponentRegistryV3.INSTANCE.getOrCreate(
            NameSpaces.MOD.path("star_models"),
            StarModelsComponent::class.java
        )

        @JvmField
        val AUTH_MODELS: ComponentKey<AuthModelsComponent> = ComponentRegistryV3.INSTANCE.getOrCreate(
            NameSpaces.MOD.path("auth_models"),
            AuthModelsComponent::class.java
        )

        @JvmField
        val MODEL_INFO: ComponentKey<ModelInfoComponent> =
            ComponentRegistryV3.INSTANCE.getOrCreate(NameSpaces.MOD.path("model_info"), ModelInfoComponent::class.java)

        @JvmField
        val PROJECTILE_MODEL: ComponentKey<ProjectileModelComponent> = ComponentRegistryV3.INSTANCE.getOrCreate(
            NameSpaces.MOD.path("projectile_model"),
            ProjectileModelComponent::class.java
        )

        @JvmField
        val VEHICLE_MODEL: ComponentKey<VehicleModelComponent> = ComponentRegistryV3.INSTANCE.getOrCreate(
            NameSpaces.MOD.path("vehicle_model"),
            VehicleModelComponent::class.java
        )
    }

    override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
        registry.registerForPlayers(STAR_MODELS, { StarModelsComponent() }, RespawnCopyStrategy.ALWAYS_COPY)
        registry.registerForPlayers(AUTH_MODELS, { AuthModelsComponent() }, RespawnCopyStrategy.ALWAYS_COPY)
        registry.registerForPlayers(MODEL_INFO, { ModelInfoComponent() }, RespawnCopyStrategy.ALWAYS_COPY)
        registry.registerFor(Projectile::class.java, PROJECTILE_MODEL) { ProjectileModelComponent() }
        registry.registerFor(Entity::class.java, VEHICLE_MODEL) { VehicleModelComponent() }
    }
}
