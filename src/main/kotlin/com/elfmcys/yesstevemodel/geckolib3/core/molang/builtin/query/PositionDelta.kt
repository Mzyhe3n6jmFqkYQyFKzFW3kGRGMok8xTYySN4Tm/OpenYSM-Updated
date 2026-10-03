package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.query

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.EntityFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

open class PositionDelta : EntityFunction() {
    open fun eval(context: ExecutionContext<IContext<Entity>>, arguments: ArgumentCollection): Any {
        var value: Int = arguments.getAsInt(context, 0)
        var vec3: Vec3 = context.entity().geoInstance().getPositionTracker().getPositionDelta()
    }
    open fun validateArgumentSize(size: Int): Boolean {
        return size == 1
    }
}