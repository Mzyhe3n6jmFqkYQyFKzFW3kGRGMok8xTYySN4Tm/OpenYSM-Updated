@file:Suppress("unused")

package com.elfmcys.yesstevemodel.capability

import com.elfmcys.yesstevemodel.capability.fabric.VehicleCapabilityImpl
import com.elfmcys.yesstevemodel.client.entity.GeckoVehicleEntity
import com.elfmcys.yesstevemodel.molang.runtime.Int2FloatOpenHashMapStruct
import it.unimi.dsi.fastutil.ints.Int2FloatMap
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.world.entity.Entity

@Environment(EnvType.CLIENT)
class VehicleCapability(entity: Entity) : GeckoVehicleEntity(entity) {
    private var floatProperties: Int2FloatOpenHashMapStruct? = null

    fun setOwnerModelId(str: String) {
        modelId = str
        markModelInitialized()
    }

    fun setFloatMap(int2FloatOpenHashMap: Int2FloatOpenHashMap) {
        floatProperties = Int2FloatOpenHashMapStruct(int2FloatOpenHashMap)
    }

    fun updateFloatMap(int2FloatMap: Int2FloatMap) {
        val props = floatProperties ?: Int2FloatOpenHashMapStruct(Int2FloatOpenHashMap()).also {
            floatProperties = it
        }
        props.merge(int2FloatMap)
    }

    override fun setupAnim(seekTime: Float, isFirstPerson: Boolean) {
        super.setupAnim(seekTime, isFirstPerson)
        getEvaluationContext().setRoamingProperties(floatProperties)
    }

    companion object {
        @JvmStatic
        operator fun get(entity: Entity): VehicleCapability? = VehicleCapabilityImpl[entity]
    }
}
