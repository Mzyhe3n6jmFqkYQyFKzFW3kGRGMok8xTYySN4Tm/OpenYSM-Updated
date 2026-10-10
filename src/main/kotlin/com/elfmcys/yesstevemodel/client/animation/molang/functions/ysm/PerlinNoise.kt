package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.EntityFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function.ArgumentCollection
import net.minecraft.world.entity.Entity
import org.lwjgl.stb.STBPerlin

class PerlinNoise : EntityFunction() {
    override fun eval(context: ExecutionContext<IContext<Entity>>, arguments: ArgumentCollection): Any {
        val seed: Int = arguments.getAsInt(context, 0)
        val x: Float = arguments.getAsFloat(context, 1)
        val y: Float = if (arguments.size() > 2) arguments.getAsFloat(context, 2) else 0.0f
        val z: Float = if (arguments.size() > 3) arguments.getAsFloat(context, 3) else 0.0f
        return STBPerlin.stb_perlin_noise3_seed(x, y, z, 0, 0, 0, seed)
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size >= 2
    }
}