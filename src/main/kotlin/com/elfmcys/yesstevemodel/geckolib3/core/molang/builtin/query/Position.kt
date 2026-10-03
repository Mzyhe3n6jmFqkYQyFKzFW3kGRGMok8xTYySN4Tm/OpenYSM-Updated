package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.query

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.EntityFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity

class Position : EntityFunction() {
    override fun eval(context: ExecutionContext<IContext<Entity>>, arguments: Function.ArgumentCollection): Any? {
        val value: Int = arguments.getAsInt(context, 0)
        val partialTicks: Float = context.entity().animationEvent().getFrameTime()
        val entity: Entity = context.entity().entity()
        return when (value) {
            0 -> Mth.lerp(partialTicks.toDouble(), entity.xo, entity.x)
            1 -> Mth.lerp(partialTicks.toDouble(), entity.yo, entity.y)
            2 -> Mth.lerp(partialTicks.toDouble(), entity.zo, entity.z)
            else -> null
        }
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size == 1
    }
}