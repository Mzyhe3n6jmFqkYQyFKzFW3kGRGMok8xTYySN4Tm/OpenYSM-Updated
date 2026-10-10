package com.elfmcys.yesstevemodel.client.animation.molang.functions.ctrl

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.LivingEntityFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function.ArgumentCollection
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.LivingEntity

class Ride : LivingEntityFunction() {
    override fun eval(context: ExecutionContext<IContext<LivingEntity>>, arguments: ArgumentCollection): Any {
        val type = arguments.getAsString(context, 0) ?: return MODE_VEHICLE
        val id = arguments.getAsString(context, 1) ?: return MODE_VEHICLE
        val entity = context.entity.entity
        if (id.isBlank()) return MODE_VEHICLE
        val targetEntity = when (type) {
            VEHICLE_KEY -> entity.vehicle
            PASSENGER_KEY -> entity.firstPassenger
            else -> return MODE_VEHICLE
        }
        if (targetEntity == null || !targetEntity.isAlive) return MODE_VEHICLE
        val strSubstring = id.substring(1)
        val entityType = targetEntity.type
        if (id.startsWith(PREFIX_ITEM_ID)) {
            val key = BuiltInRegistries.ENTITY_TYPE.getKey(entityType)
            return if (strSubstring == key.toString()) MODE_PASSENGER else MODE_VEHICLE
        }
        if (id.startsWith(PREFIX_ITEM_TAG))
            return if (entityType.`is`(
                    TagKey.create(
                        Registries.ENTITY_TYPE,
                        Identifier.parse(strSubstring)
                    )
                )
            ) MODE_PASSENGER else MODE_VEHICLE
        return MODE_VEHICLE
    }

    override fun validateArgumentSize(size: Int): Boolean = size == 2 || size == 3

    companion object {
        const val PREFIX_ITEM_ID = "$"
        const val PREFIX_ITEM_TAG = "#"
        const val VEHICLE_KEY = "vehicle"
        const val PASSENGER_KEY = "passenger"
        const val MODE_VEHICLE = 0
        const val MODE_PASSENGER = 1

        fun create(): Ride = Ride()
    }
}