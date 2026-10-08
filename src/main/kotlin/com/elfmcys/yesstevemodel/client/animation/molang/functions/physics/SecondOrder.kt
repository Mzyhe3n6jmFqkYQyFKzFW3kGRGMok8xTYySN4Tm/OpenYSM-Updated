package com.elfmcys.yesstevemodel.client.animation.molang.functions.physics

import net.minecraft.util.Mth
import kotlin.math.ceil
import kotlin.math.sqrt

class SecondOrder(
    private var input: Float,
    frequency: Float,
    coefficient: Float,
    private var response: Float
) : IPhysics {
    private var inputFunction: Float = 0.0f
    private var lastSimulation: Float = 0.0f
    private var lastSimulationDot: Float = 0.0f
    private var frequency: Float = Mth.clamp(frequency, 0.0f, 5.0f)
    private var coefficient: Float = Mth.clamp(coefficient, 0.0f, 1.0f)

    override fun update(timeStep: Float) {
        var step = timeStep
        val input = this.input
        val freq = Mth.clamp(frequency, 0.0f, 5.0f)
        val coeff = Mth.clamp(coefficient, 0.0f, 1.0f)
        val resp = this.response

        val k1 = coeff / Mth.PI / freq
        val k2 = 1.0f / (2.0f * Mth.PI * freq) / (2.0f * Mth.PI * freq)
        val k3 = resp * coeff / 2.0f / Mth.PI / freq

        val inputFunctionDot = (input - inputFunction) / step
        inputFunction = input

        val maxTimeStep = sqrt(4.0f * k2 + k1 * k1) - k1
        var cycleTime = ceil(step / maxTimeStep).toInt()
        step /= cycleTime

        var currentSimDot = lastSimulationDot
        var currentSim = lastSimulation
        while (cycleTime > 0) {
            currentSim += step * currentSimDot
            currentSimDot += step * (k3 * inputFunctionDot + input - currentSim - k1 * currentSimDot) / k2
            cycleTime--
        }
        lastSimulation = currentSim
        lastSimulationDot = currentSimDot
    }

    override fun setArgs(arg0: Float, arg1: Float, arg2: Float, arg3: Float) {
        input = arg0
        frequency = arg1
        coefficient = arg2
        response = arg3
    }

    override val value: Float
        get() = lastSimulation
}