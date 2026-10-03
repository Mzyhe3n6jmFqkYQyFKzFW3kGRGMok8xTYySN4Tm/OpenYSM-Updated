package com.elfmcys.yesstevemodel.geckolib3.util

import net.minecraft.world.phys.Vec3
import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.lang3.Validate
import org.joml.Vector3f

object VectorUtils {
    @JvmStatic
    fun fromArray(array: DoubleArray): Vec3 {
        Validate.validIndex(ArrayUtils.toObject(array), 2)
        return Vec3(array[0], array[1], array[2])
    }

    @JvmStatic
    fun fromArray(array: FloatArray): Vector3f {
        Validate.validIndex(ArrayUtils.toObject(array), 2)
        return Vector3f(array[0], array[1], array[2])
    }

    @JvmStatic
    fun convertDoubleToFloat(vector: Vec3): Vector3f {
        return Vector3f(vector.x.toFloat(), vector.y.toFloat(), vector.z.toFloat())
    }

    @JvmStatic
    fun convertFloatToDouble(vector: Vector3f): Vec3 {
        return Vec3(vector.x().toDouble(), vector.y().toDouble(), vector.z().toDouble())
    }
}