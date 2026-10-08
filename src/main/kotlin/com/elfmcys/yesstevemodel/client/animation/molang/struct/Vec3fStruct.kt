package com.elfmcys.yesstevemodel.client.animation.molang.struct

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.molang.runtime.Struct

abstract class Vec3fStruct : Struct {
    override fun get(name: Int): Any? {
        return when (name) {
            NAME_X -> getX()
            NAME_Y -> getY()
            NAME_Z -> getZ()
            else -> null
        }
    }

    abstract fun getX(): Float
    abstract fun getY(): Float
    abstract fun getZ(): Float

    override fun set(name: Int, value: Any?) {}

    override fun toString(): String {
        return String.format("vec3{x=%.2f, y=%.2f, z=%.2f}", getX(), getY(), getZ())
    }

    override fun copy(): Struct {
        return this
    }

    companion object {
        private val NAME_X: Int = StringPool.computeIfAbsent("x")
        private val NAME_Y: Int = StringPool.computeIfAbsent("y")
        private val NAME_Z: Int = StringPool.computeIfAbsent("z")
    }
}