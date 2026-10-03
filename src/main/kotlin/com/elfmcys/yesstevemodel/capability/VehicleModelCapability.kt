@file:Suppress("unused")

package com.elfmcys.yesstevemodel.capability

import com.elfmcys.yesstevemodel.capability.fabric.VehicleModelCapabilityImpl
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.Entity

class VehicleModelCapability {
    private var ownerModelId: String = "default"
    private var initialized: Boolean = false
    private var molangVars: Object2FloatOpenHashMap<String> = Object2FloatOpenHashMap()

    fun setModel(str: String, object2FloatOpenHashMap: Object2FloatOpenHashMap<String>) {
        ownerModelId = str
        initialized = true
        molangVars = object2FloatOpenHashMap
    }

    fun copyFrom(other: VehicleModelCapability) {
        ownerModelId = other.ownerModelId
        initialized = other.initialized
        molangVars = other.molangVars
    }

    fun getOwnerModelId(): String = ownerModelId

    fun isInitialized(): Boolean = initialized

    fun getMolangVars(): Object2FloatOpenHashMap<String> = molangVars

    fun serializeNBT(): CompoundTag {
        val compoundTag = CompoundTag()
        compoundTag.putString("owner_model_id", ownerModelId)
        compoundTag.putBoolean("initialized", initialized)
        val compoundTag2 = CompoundTag()
        molangVars.object2FloatEntrySet().fastForEach { entry ->
            compoundTag2.putFloat(entry.key, entry.floatValue)
        }
        compoundTag.put("molang_vars_server_bound", compoundTag2)
        return compoundTag
    }

    fun deserializeNBT(compoundTag: CompoundTag) {
        ownerModelId = compoundTag.getStringOr("owner_model_id", "default")
        initialized = compoundTag.getBooleanOr("initialized", false)
        molangVars.clear()
        val compound = compoundTag.getCompoundOrEmpty("molang_vars_server_bound")
        for (str in compound.keySet()) {
            molangVars.put(str, compound.getFloatOr(str, 0.0f))
        }
    }

    companion object {
        @JvmStatic
        operator fun get(entity: Entity): VehicleModelCapability? = VehicleModelCapabilityImpl[entity]
    }
}
