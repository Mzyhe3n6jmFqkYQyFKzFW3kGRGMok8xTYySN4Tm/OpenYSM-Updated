package com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function

abstract class ContextFunction<TEntity> : Function {
    open fun validateContext(context: IContext<*>): Boolean {
        return true
    }

    @Suppress("UNCHECKED_CAST")
    override fun evaluate(context: ExecutionContext<*>, arguments: Function.ArgumentCollection): Any? {
        val entity: Any? = context.entity()
        if (entity is IContext<*> && validateContext(entity)) {
            return eval(context as ExecutionContext<IContext<TEntity>>, arguments)
        }
        return null
    }

    abstract fun eval(context: ExecutionContext<IContext<TEntity>>, arguments: Function.ArgumentCollection): Any?
}