package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.query

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.EntityFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity

open class Position : EntityFunction() {
    open fun eval(context: ExecutionContext<IContext<Entity>>, arguments: ArgumentCollection): Any {
        var value: Int = arguments.getAsInt(context, 0)
        var partialTicks: Float = context.entity().animationEvent().getFrameTime()
        var entity: Entity = context.entity().entity()
    }
    open fun validateArgumentSize(size: Int): Boolean {
        return size == 1
    }
}