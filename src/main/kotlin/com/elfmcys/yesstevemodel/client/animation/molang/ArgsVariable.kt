package com.elfmcys.yesstevemodel.client.animation.molang

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Variable

object ArgsVariable : Variable {
    override fun evaluate(context: ExecutionContext<*>): Any? {
        val entity = context.entity
        if (entity is IContext<*>) {
            return entity.animationLayers
        }
        return null
    }
}