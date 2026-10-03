package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.EntityFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function.ArgumentCollection
import com.elfmcys.yesstevemodel.util.ParticleEffectUtil
import net.minecraft.world.entity.Entity

class Particle(private val abs: Boolean) : EntityFunction() {
    override fun eval(context: ExecutionContext<IContext<Entity>>, arguments: ArgumentCollection): Any? {
        if (!context.entity().isClientSide() || context.entity().geoInstance().hasCustomTexture()) {
            return null
        }
        return runCatching {
            ParticleEffectUtil.handleParticle(context, arguments, abs)
        }.getOrThrow()
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size >= 1
    }
}