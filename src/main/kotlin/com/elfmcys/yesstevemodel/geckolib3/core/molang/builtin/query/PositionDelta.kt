package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.query

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.EntityFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

class PositionDelta : EntityFunction() {
    override fun eval(context: ExecutionContext<IContext<Entity>>, arguments: Function.ArgumentCollection): Any? {
        val value: Int = arguments.getAsInt(context, 0)
        val vec3: Vec3 = context.entity().geoInstance().positionTracker.getPositionDelta()
        return when (value) {
            0 -> vec3.x
            1 -> vec3.y
            2 -> vec3.z
            else -> null
        }
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size == 1
    }
}