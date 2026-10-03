package com.elfmcys.yesstevemodel.client.animation.molang.functions.physics

class FirstOrder(
    private var input: Float,
    private var response: Float
) : IPhysics {
    private var lastSimulation: Float = 0.0f

    override fun update(timeStep: Float) {
        lastSimulation = ((1.0f - (timeStep / response)) * lastSimulation) + ((timeStep / response) * input)
    }

    override fun setArgs(arg0: Float, arg1: Float, arg2: Float, arg3: Float) {
        input = arg0
        response = arg1
    }

    override fun getValue(): Float = lastSimulation
}