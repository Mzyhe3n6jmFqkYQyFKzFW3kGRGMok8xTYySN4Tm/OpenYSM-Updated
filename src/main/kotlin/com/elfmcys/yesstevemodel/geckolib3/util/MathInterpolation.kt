package com.elfmcys.yesstevemodel.geckolib3.util

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.util.MathUtil
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object MathInterpolation {
    @JvmStatic
    fun getYawInterpolation(context: IContext<Entity>): Double {
        val entity = context.entity
        val frameTime = context.animationEvent.frameTime
        val positionDelta = context.geoInstance.positionTracker.positionDelta
        val d = positionDelta.x
        val d2 = positionDelta.z
        if (sqrt(d * d + d2 * d2) < 1.0E-4) return 0.0
        val angleDeg = MathUtil.radiansToDegrees(Mth.atan2(d2, d).toFloat()) - (90.0f - Mth.wrapDegrees(
            -entity.getViewYRot(frameTime)
        ))
        val rad = MathUtil.degreesToRadians(Mth.wrapDegrees(angleDeg))
        return cos(rad.toDouble())
    }

    @JvmStatic
    fun getPitchInterpolation(context: IContext<Entity>): Double {
        val entity = context.entity
        val frameTime = context.animationEvent.frameTime
        val positionDelta = context.geoInstance.positionTracker.positionDelta
        val d = positionDelta.x
        val d2 = positionDelta.z
        if (sqrt(d * d + d2 * d2) < 1.0E-4) return 0.0
        val angleDeg = MathUtil.radiansToDegrees(Mth.atan2(d2, d).toFloat()) - (90.0f - Mth.wrapDegrees(
            -entity.getViewYRot(frameTime)
        ))
        val rad = MathUtil.degreesToRadians(Mth.wrapDegrees(angleDeg))
        return sin(rad.toDouble())
    }
}