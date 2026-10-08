package com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.entity

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.IValueEvaluator
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.LambdaVariable
import net.minecraft.client.player.LocalPlayer

open class LocalPlayerEntityVariable(evaluator: IValueEvaluator<*, IContext<LocalPlayer>>) :
    LambdaVariable<LocalPlayer>(evaluator) {
    override fun validateContext(context: IContext<*>): Boolean {
        return context.entity is LocalPlayer
    }
}