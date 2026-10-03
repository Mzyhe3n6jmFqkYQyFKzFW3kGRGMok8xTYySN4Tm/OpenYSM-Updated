package com.elfmcys.yesstevemodel.geckolib3.core.molang.variable

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Variable

abstract class ContextVariable<TEntity> : Variable {
    protected open fun validateContext(context: IContext<*>): Boolean {
        return true
    }

    @Suppress("UNCHECKED_CAST")
    override fun evaluate(context: ExecutionContext<*>): Any? {
        val entity = context.entity()
        if (entity is IContext<*> && validateContext(entity)) return evaluate(entity as IContext<TEntity>)
        return null
    }

    abstract fun evaluate(context: IContext<TEntity>): Any?
}