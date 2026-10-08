package com.elfmcys.yesstevemodel.client.animation.molang.functions.physics

interface IPhysics {
    fun update(timeStep: Float)
    fun setArgs(arg0: Float, arg1: Float, arg2: Float, arg3: Float)
    val value: Float
}