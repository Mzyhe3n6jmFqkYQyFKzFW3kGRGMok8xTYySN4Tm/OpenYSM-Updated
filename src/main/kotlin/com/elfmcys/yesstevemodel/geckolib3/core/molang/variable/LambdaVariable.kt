package com.elfmcys.yesstevemodel.geckolib3.core.molang.variable

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext

open class LambdaVariable<TTarget>(
    private val evaluator: IValueEvaluator<*, IContext<TTarget>>
) : ContextVariable<TTarget>() {
    override fun evaluate(context: IContext<TTarget>): Any? {
        return evaluator.eval(context)
    }
}