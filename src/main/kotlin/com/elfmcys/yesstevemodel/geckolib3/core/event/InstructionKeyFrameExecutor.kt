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
            val keyFrame = list[nextIndex]
            if (keyFrame.startTick > currentTick) {
                break
            }
            evalValues(evaluator, keyFrame.eventData)
            nextIndex++
        }
        evaluator.entity().setIsClientSide(false)
    }

    open fun executeRemaining(evaluator: ExpressionEvaluator<AnimationContext<*>>, isClientSide: Boolean) {
        evaluator.entity().setIsClientSide(isClientSide)
        for (i in nextIndex until list.size) {
            evalValues(evaluator, list[i].getEventData())
        }
        evaluator.entity().setIsClientSide(false)
        nextIndex = list.size
    }

    open fun reachEnd(): Boolean = nextIndex >= list.size

    open fun reset() {
        nextIndex = 0
    }
}