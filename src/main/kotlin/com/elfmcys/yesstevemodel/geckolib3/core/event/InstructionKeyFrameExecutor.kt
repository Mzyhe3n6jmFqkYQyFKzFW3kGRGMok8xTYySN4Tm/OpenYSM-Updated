package com.elfmcys.yesstevemodel.geckolib3.core.event

import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.event.EventKeyFrame
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator

open class InstructionKeyFrameExecutor(private val list: MutableList<EventKeyFrame<Array<IValue>>>) {
    private var nextIndex: Int = 0

    open fun evalValues(evaluator: ExpressionEvaluator<*>, values: Array<IValue>) {
        for (value in values) {
            value.evalSafe(evaluator)
        }
    }

    open fun executeTo(evaluator: ExpressionEvaluator<AnimationContext<*>>, currentTick: Float, isClientSide: Boolean) {
        evaluator.entity().setIsClientSide(isClientSide)
        while (!reachEnd()) {
            val keyFrame: EventKeyFrame<Array<IValue>> = list.get(nextIndex)
            if (keyFrame.getStartTick() > currentTick) {
                break
            }
            evalValues(evaluator, keyFrame.getEventData())
            nextIndex++
        }
        evaluator.entity().setIsClientSide(false)
    }

    open fun executeRemaining(evaluator: ExpressionEvaluator<AnimationContext<*>>, isClientSide: Boolean) {
        evaluator.entity().setIsClientSide(isClientSide)
        for (i in nextIndex until list.size) {
            evalValues(evaluator, list.get(i).getEventData())
        }
        evaluator.entity().setIsClientSide(false)
        nextIndex = list.size
    }

    open fun reachEnd(): Boolean = nextIndex >= list.size

    open fun reset() {
        nextIndex = 0
    }
}