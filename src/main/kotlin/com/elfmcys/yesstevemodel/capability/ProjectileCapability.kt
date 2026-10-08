@file:Suppress("unused")

package com.elfmcys.yesstevemodel.capability

import com.elfmcys.yesstevemodel.capability.fabric.ProjectileCapabilityImpl
import com.elfmcys.yesstevemodel.client.entity.GeckoProjectileEntity
import com.elfmcys.yesstevemodel.molang.runtime.Int2FloatOpenHashMapStruct
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.projectile.Projectile

@Environment(EnvType.CLIENT)
class ProjectileCapability(projectile: Projectile) : GeckoProjectileEntity(projectile) {
    private var floatProperties: Int2FloatOpenHashMapStruct? = null

    fun updateModelId(str: String) {
        modelId = str
        markModelInitialized()
    }

    fun setFloatProperties(int2FloatOpenHashMap: Int2FloatOpenHashMap?) {
        floatProperties = if (int2FloatOpenHashMap != null) Int2FloatOpenHashMapStruct(int2FloatOpenHashMap) else null
    }

    override fun setupAnim(seekTime: Float, isFirstPerson: Boolean) {
        super.setupAnim(seekTime, isFirstPerson)
        getEvaluationContext().setRoamingProperties(floatProperties)
    }

    companion object {
        @JvmStatic
        operator fun get(entity: Entity): ProjectileCapability? = ProjectileCapabilityImpl[entity]

        @JvmStatic
        operator fun get(projectile: Projectile): ProjectileCapability? = ProjectileCapabilityImpl[projectile]
    }
}
