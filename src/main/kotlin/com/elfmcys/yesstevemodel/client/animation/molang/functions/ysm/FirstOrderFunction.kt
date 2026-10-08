package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm

import com.elfmcys.yesstevemodel.client.animation.molang.functions.physics.FirstOrder
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.EntityFunction
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function.ArgumentCollection
import net.minecraft.world.entity.Entity

class FirstOrderFunction : EntityFunction() {
    override fun eval(context: ExecutionContext<IContext<Entity>>, arguments: ArgumentCollection): Any {
        val name = arguments.getStringId(context, 0)
        if (name == StringPool.EMPTY_ID) return 0
        val input = arguments.getAsFloat(context, 1)
        val response = if (arguments.size() >= 3) arguments.getAsFloat(context, 2) else 1.0f
        val physicsManager = context.entity().geoInstance().getPhysicsManager()
        val physics = physicsManager[name]
        if (physics == null) {
            physicsManager[name] = FirstOrder(input, response)
            return input
        }
        physics.setArgs(input, response, 0.0f, 0.0f)
        return physics.value
    }

    override fun validateArgumentSize(size: Int): Boolean = size >= 2
}