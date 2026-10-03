package com.elfmcys.yesstevemodel.geckolib3.util

interface IInterpolable {
    fun interpolate(f: Float): Float
    fun getProgress(): Float
    fun asInterpolator(): IInterpolable {
        return this
    }
}