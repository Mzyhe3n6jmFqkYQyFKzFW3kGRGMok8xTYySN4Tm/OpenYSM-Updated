package com.elfmcys.yesstevemodel.geckolib3.core.util

import net.minecraft.util.Mth
import org.joml.Math
import org.joml.Quaternionf
import org.joml.Vector3f

object MathUtil {
    private const val DEGREES_TO_RADIANS: Float = Mth.DEG_TO_RAD

    private const val RADIANS_TO_DEGREES: Float = Mth.RAD_TO_DEG
    const val PI_FROM_DEG: Float = 3.1415927f

    val TWO_PI: Float = Math.toRadians(360.0).toFloat()

    val PI: Float = Math.toRadians(180.0).toFloat()

    val ZERO: Vector3f = Vector3f(0.0f, 0.0f, 0.0f)

    val ONE: Vector3f = Vector3f(1.0f, 1.0f, 1.0f)

    fun eulerZYXToQuaternion(angles: Vector3f): Quaternionf = Quaternionf().rotateZYX(angles.z, angles.y, angles.x)

    fun nlerpEulerAngles(
        percentCompleted: Float,
        startEuler: Vector3f,
        endEuler: Vector3f,
        offsetEuler: Vector3f,
        outEuler: Vector3f
    ) {
        nlerpEulerAngles(percentCompleted, startEuler, endEuler, offsetEuler, outEuler, EulerNlerpScratch())
    }

    fun nlerpEulerAngles(
        percentCompleted: Float,
        startEuler: Vector3f,
        endEuler: Vector3f,
        offsetEuler: Vector3f,
        outEuler: Vector3f,
        scratch: EulerNlerpScratch
    ) {
        val tempEuler: Vector3f = scratch.vec
        val startQuat: Quaternionf = scratch.qa
        val endQuat: Quaternionf = scratch.qb
        startEuler.add(offsetEuler, tempEuler)
        startQuat.identity().rotateZYX(tempEuler.z, tempEuler.y, tempEuler.x)
        endEuler.add(offsetEuler, tempEuler)
        endQuat.identity().rotateZYX(tempEuler.z, tempEuler.y, tempEuler.x)
        startQuat.nlerp(endQuat, percentCompleted, endQuat)
        getEulerAnglesZYX(endQuat, tempEuler)
        tempEuler.sub(offsetEuler, outEuler)
    }

    fun getEulerAnglesZYX(quaternionf: Quaternionf, eulerAngles: Vector3f): Vector3f {
        eulerAngles.x = Math.atan2(
            quaternionf.y * quaternionf.z + quaternionf.w * quaternionf.x,
            0.5f - quaternionf.x * quaternionf.x - quaternionf.y * quaternionf.y
        )
        eulerAngles.y = Math.safeAsin(-2.0f * (quaternionf.x * quaternionf.z - quaternionf.w * quaternionf.y))
        eulerAngles.z = Math.atan2(
            quaternionf.x * quaternionf.y + quaternionf.w * quaternionf.z,
            0.5f - quaternionf.y * quaternionf.y - quaternionf.z * quaternionf.z
        )
        return eulerAngles
    }

    fun lerpValues(percentCompleted: Float, begin: Vector3f, end: Vector3f): Vector3f {
        return Vector3f(
            lerpValues(percentCompleted, begin.x(), end.x()),
            lerpValues(percentCompleted, begin.y(), end.y()),
            lerpValues(percentCompleted, begin.z(), end.z())
        )
    }

    fun lerpValues(percentCompleted: Float, begin: Vector3f, end: Vector3f, outResult: Vector3f) {
        outResult.set(
            lerpValues(percentCompleted, begin.x(), end.x()),
            lerpValues(percentCompleted, begin.y(), end.y()),
            lerpValues(percentCompleted, begin.z(), end.z())
        )
    }

    fun lerpValues(percentCompleted: Float, startValue: Float, endValue: Float): Float {
        return startValue + percentCompleted * (endValue - startValue)
    }

    fun catmullRom(percentCompleted: Float, left: Vector3f, begin: Vector3f, end: Vector3f, right: Vector3f): Vector3f {
        return Vector3f(
            catmullRom(percentCompleted, left.x(), begin.x(), end.x(), right.x()),
            catmullRom(percentCompleted, left.y(), begin.y(), end.y(), right.y()),
            catmullRom(percentCompleted, left.z(), begin.z(), end.z(), right.z())
        )
    }

    fun catmullRom(percent: Float, left: Float, begin: Float, end: Float, right: Float): Float {
        val v0: Float = (end - left) * 0.5f
        val v1: Float = (right - begin) * 0.5f
        val t2: Float = percent * percent
        val t3: Float = percent * t2
        return (2 * begin - 2 * end + v0 + v1) * t3 + (-3 * begin + 3 * end - 2 * v0 - v1) * t2 + v0 * percent + begin
    }

    fun degreesToRadians(degrees: Float): Float {
        return degrees * DEGREES_TO_RADIANS
    }

    fun radiansToDegrees(degrees: Float): Float {
        return degrees * RADIANS_TO_DEGREES
    }

    fun normalizeAnglesInPlace(source: Vector3f, outResult: Vector3f) {
        outResult.set(normalizeAngle(source.x), normalizeAngle(source.y), normalizeAngle(source.z))
    }

    fun normalizeAngles(angles: Vector3f): Vector3f {
        return Vector3f(normalizeAngle(angles.x), normalizeAngle(angles.y), normalizeAngle(angles.z))
    }

    fun normalizeAngle(angle: Float): Float {
        var f2: Float = angle % TWO_PI
        if (f2 >= PI) {
            f2 -= TWO_PI
        }
        if (f2 < -PI) {
            f2 += TWO_PI
        }
        return f2
    }

    fun lerpAngles(targetAngles: Vector3f, t: Float): Vector3f {
        return Vector3f(lerpAngle(targetAngles.x, t), lerpAngle(targetAngles.y, t), lerpAngle(targetAngles.z, t))
    }

    fun lerpAnglesInPlace(targetAngles: Vector3f, t: Float, outResult: Vector3f) {
        outResult.set(lerpAngle(targetAngles.x, t), lerpAngle(targetAngles.y, t), lerpAngle(targetAngles.z, t))
    }

    fun lerpAngle(target: Float, t: Float): Float = 1.0f + (target - 1.0f) * t
}