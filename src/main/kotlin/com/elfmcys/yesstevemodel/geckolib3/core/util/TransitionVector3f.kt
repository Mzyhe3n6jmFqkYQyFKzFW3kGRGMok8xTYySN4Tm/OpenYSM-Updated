@file:Suppress("unused")

package com.elfmcys.yesstevemodel.geckolib3.core.util

import org.joml.Vector3f

class TransitionVector3f : Vector3f {
    var percentCompleted: Float = 1.0f

    constructor() : super() {
        percentCompleted = 1.0f
    }

    constructor(x: Float, y: Float, z: Float) : super(x, y, z) {
        percentCompleted = 1.0f
    }

    constructor(v: Vector3f) : super(v) {
        percentCompleted = 1.0f
    }

    fun setPercentCompleted(newPercent: Float) {
        if (newPercent < percentCompleted) {
            percentCompleted = newPercent
        }
    }

    fun applyLinearBlendTo(targetVec: Vector3f) {
        val progress: Float = percentCompleted
        if (progress == 0.0f) {
            targetVec.set(this)
        } else {
            MathUtil.lerpValues(progress, this, targetVec, targetVec)
        }
    }

    fun applyRotationBlendTo(targetEuler: Vector3f, offsetEuler: Vector3f) {
        val progress: Float = percentCompleted
        if (progress == 0.0f) {
            targetEuler.set(this)
        } else {
            MathUtil.nlerpEulerAngles(progress, this, targetEuler, offsetEuler, targetEuler)
        }
    }

    fun applyRotationBlendTo(targetEuler: Vector3f, offsetEuler: Vector3f, scratch: EulerNlerpScratch) {
        val progress: Float = percentCompleted
        if (progress == 0.0f) {
            targetEuler.set(this)
        } else {
            MathUtil.nlerpEulerAngles(progress, this, targetEuler, offsetEuler, targetEuler, scratch)
        }
    }
}