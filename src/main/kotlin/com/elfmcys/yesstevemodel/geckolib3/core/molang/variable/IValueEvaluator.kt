package com.elfmcys.yesstevemodel.geckolib3.core.molang.variable

fun interface IValueEvaluator<TValue, TContext> {
    fun eval(ctx: TContext): TValue
}