package com.elfmcys.yesstevemodel.client.animation.molang.functions.ctrl

import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType
import com.elfmcys.yesstevemodel.geckolib3.core.controller.PredicateBasedController
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function.ArgumentCollection

class SetAnimation : ContextFunction<Any>() {
    override fun eval(context: ExecutionContext<IContext<Any>>, arguments: ArgumentCollection): Any? {
        val animationController: PredicateBasedController<*>? = context.entity.animationEvent.controller
        if (animationController == null) {
            return null
        }
        val animationName: String? = arguments.getAsString(context, 0)
        if (animationName.isNullOrEmpty()) {
            return null
        }
        val loopType: ILoopType? = if (arguments.size() == 1) {
            null
        } else {
            when (arguments.getAsInt(context, 1)) {
                10 -> ILoopType.EDefaultLoopTypes.LOOP
                11 -> ILoopType.EDefaultLoopTypes.PLAY_ONCE
                12 -> ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME
                else -> null
            }
        }
        animationController.setAnimation(animationName, loopType)
        return null
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size == 1 || size == 2
    }
}