package com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.entity

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.IValueEvaluator
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.LambdaVariable
import net.minecraft.client.player.AbstractClientPlayer

open class ClientPlayerEntityVariable(evaluator: IValueEvaluator<*, IContext<AbstractClientPlayer>>) :
    LambdaVariable<AbstractClientPlayer>(evaluator) {
    override fun validateContext(context: IContext<*>): Boolean {
        return context.entity() is AbstractClientPlayer
    }
}