package com.elfmcys.yesstevemodel.geckolib3.util

interface IInterpolable {
    fun interpolate(f: Float): Float
    val progress: Float
    fun asInterpolator(): IInterpolable = this
}