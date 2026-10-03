package com.elfmcys.yesstevemodel.client.animation.molang.functions.ctrl

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.LivingEntityFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function.ArgumentCollection
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity

class Ride : LivingEntityFunction() {
    override fun eval(context: ExecutionContext<IContext<LivingEntity>>, arguments: ArgumentCollection): Any? {
        val type: String = arguments.getAsString(context, 0) ?: return 0
        val id: String = arguments.getAsString(context, 1) ?: return 0
        val entity: LivingEntity = context.entity().entity()
        if (id.isBlank()) {
            return 0
        }
        val targetEntity: Entity? = when (type) {
            VEHICLE_KEY -> entity.vehicle
            PASSENGER_KEY -> entity.firstPassenger
            else -> return 0
        }
        if (targetEntity == null || !targetEntity.isAlive) {
            return 0
        }
        val strSubstring: String = id.substring(1)
        val entityType: EntityType<*> = targetEntity.type
        if (id.startsWith(PREFIX_ITEM_ID)) {
            val key: Identifier? = BuiltInRegistries.ENTITY_TYPE.getKey(entityType) ?: return 0
            return if (strSubstring == key.toString()) 1 else 0
        }
        if (id.startsWith(PREFIX_ITEM_TAG)) {
            return if (entityType.`is`(TagKey.create(Registries.ENTITY_TYPE, Identifier.parse(strSubstring)))) 1 else 0
        }
        return 0
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size == 2 || size == 3
    }

    companion object {
        const val PREFIX_ITEM_ID: String = "$"
        const val PREFIX_ITEM_TAG: String = "#"
        const val VEHICLE_KEY: String = "vehicle"
        const val PASSENGER_KEY: String = "passenger"
        const val MODE_VEHICLE: Int = 0
        const val MODE_PASSENGER: Int = 1

        @JvmStatic
        fun create(): Ride = Ride()
    }
}