package com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone

import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import org.joml.Vector3f

open class Vector3v(
    val x: IValue,
    val y: IValue,
    val z: IValue
) {
    val vector: Vector3f = Vector3f()

    open fun eval(evaluator: ExpressionEvaluator<*>): Vector3f {
        return vector.set(x.evalAsFloat(evaluator), y.evalAsFloat(evaluator), z.evalAsFloat(evaluator))
    }
}