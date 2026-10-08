@file:Suppress("unused")

package com.elfmcys.yesstevemodel.geckolib3.core.util

import org.joml.Vector3f

class TransitionVector3f : Vector3f {
    var percentCompleted: Float = 1.0f
        set(value) {
            if (value >= field) return
            field = value
        }

    constructor() : super()
    constructor(x: Float, y: Float, z: Float) : super(x, y, z)
    constructor(v: Vector3f) : super(v)

    fun setPercentCompleted(newPercent: Float) {
        if (newPercent < percentCompleted) {
            percentCompleted = newPercent
        }
    }

    fun applyLinearBlendTo(targetVec: Vector3f) {
        val progress = percentCompleted
        if (progress == 0.0f) {
            targetVec.set(this)
        } else {
            MathUtil.lerpValues(progress, this, targetVec, targetVec)
        }
    }

    fun applyRotationBlendTo(targetEuler: Vector3f, offsetEuler: Vector3f) {
        val progress = percentCompleted
        if (progress == 0.0f) {
            targetEuler.set(this)
        } else {
            MathUtil.nlerpEulerAngles(progress, this, targetEuler, offsetEuler, targetEuler)
        }
    }

    fun applyRotationBlendTo(targetEuler: Vector3f, offsetEuler: Vector3f, scratch: EulerNlerpScratch) {
        val progress = percentCompleted
        if (progress == 0.0f) {
            targetEuler.set(this)
        } else {
            MathUtil.nlerpEulerAngles(progress, this, targetEuler, offsetEuler, targetEuler, scratch)
        }
    }
}