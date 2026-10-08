@file:Suppress("unused")

package com.elfmcys.yesstevemodel.capability

import com.elfmcys.yesstevemodel.capability.fabric.ProjectileModelCapabilityImpl
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.projectile.Projectile

class ProjectileModelCapability {
    private var ownerModelId2: String = "default"
    private var initialized2: Boolean = false
    private var molangVars: Object2FloatOpenHashMap<String> = Object2FloatOpenHashMap()

    fun setModel(str: String, object2FloatOpenHashMap: Object2FloatOpenHashMap<String>) {
        ownerModelId2 = str
        initialized2 = true
        molangVars = object2FloatOpenHashMap
    }

    fun copyFrom(other: ProjectileModelCapability) {
        ownerModelId2 = other.ownerModelId2
        initialized2 = other.initialized2
        molangVars = other.molangVars
    }

    val ownerModelId: String
        get() = ownerModelId2

    val isInitialized: Boolean
        get() = initialized2

    fun getMolangVars(): Object2FloatOpenHashMap<String> = molangVars

    fun serializeNBT(): CompoundTag {
        val compoundTag = CompoundTag()
        compoundTag.putString("owner_model_id", ownerModelId2)
        compoundTag.putBoolean("initialized", initialized2)
        val compoundTag2 = CompoundTag()
        molangVars.object2FloatEntrySet().fastForEach { entry ->
            compoundTag2.putFloat(entry.key, entry.floatValue)
        }
        compoundTag.put("molang_vars_server_bound", compoundTag2)
        return compoundTag
    }

    fun deserializeNBT(compoundTag: CompoundTag) {
        ownerModelId2 = compoundTag.getStringOr("owner_model_id", "default")
        initialized2 = compoundTag.getBooleanOr("initialized", false)
        molangVars.clear()
        val compound = compoundTag.getCompoundOrEmpty("molang_vars_server_bound")
        for (str in compound.keySet()) {
            molangVars.put(str, compound.getFloatOr(str, 0.0f))
        }
    }

    companion object {
        @JvmStatic
        operator fun get(entity: Entity): ProjectileModelCapability? = ProjectileModelCapabilityImpl[entity]

        @JvmStatic
        operator fun get(projectile: Projectile): ProjectileModelCapability? = ProjectileModelCapabilityImpl[projectile]
    }
}
