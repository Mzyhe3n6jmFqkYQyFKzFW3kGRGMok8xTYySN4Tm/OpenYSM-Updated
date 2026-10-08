package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm

import com.elfmcys.yesstevemodel.client.animation.molang.PhysicsManager
import com.elfmcys.yesstevemodel.client.animation.molang.functions.physics.IPhysics
import com.elfmcys.yesstevemodel.client.animation.molang.functions.physics.SecondOrder
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.EntityFunction
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function.ArgumentCollection
import net.minecraft.world.entity.Entity

class SecondOrderFunction : EntityFunction() {
    override fun eval(context: ExecutionContext<IContext<Entity>>, arguments: ArgumentCollection): Any {
        val name: Int = arguments.getStringId(context, 0)
        if (name == StringPool.EMPTY_ID) {
            return 0
        }
        val input: Float = arguments.getAsFloat(context, 1)
        val size: Int = arguments.size()
        val frequency: Float = if (size >= 3) arguments.getAsFloat(context, 2) else 1.0f
        val coefficient: Float = if (size >= 4) arguments.getAsFloat(context, 3) else 1.0f
        val response: Float = if (size >= 5) arguments.getAsFloat(context, 4) else 1.0f
        val physicsManager: PhysicsManager = context.entity().geoInstance().getPhysicsManager()
        val physics: IPhysics? = physicsManager.get(name)
        if (physics == null) {
            physicsManager.put(name, SecondOrder(input, frequency, coefficient, response))
            return input
        }
        physics.setArgs(input, frequency, coefficient, response)
        return physics.value
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size >= 2
    }
}