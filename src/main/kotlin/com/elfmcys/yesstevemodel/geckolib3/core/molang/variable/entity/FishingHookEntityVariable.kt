package com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.entity

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.IValueEvaluator
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.LambdaVariable
import net.minecraft.world.entity.projectile.FishingHook

open class FishingHookEntityVariable(evaluator: IValueEvaluator<*, IContext<FishingHook>>) :
    LambdaVariable<FishingHook>(evaluator) {
    override fun validateContext(context: IContext<*>): Boolean {
        return context.entity is FishingHook
    }
}