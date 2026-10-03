package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin

import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ContextBinding
import com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math.*

object MathBinding : ContextBinding() {
    init {
        constValue("pi", Math.PI)
        constValue("e", Math.E)
        function("floor", Floor())
        function("round", Round())
        function("ceil", Ceil())
        function("trunc", Trunc())
        function("clamp", Clamp())
        function("max", Max())
        function("min", Min())
        function("abs", Abs())
        function("exp", Exp())
        function("ln", Ln())
        function("sqrt", Sqrt())
        function("mod", Mod())
        function("pow", Pow())
        function("sin", Sin())
        function("cos", Cos())
        function("acos", ACos())
        function("asin", ASin())
        function("atan", Atan())
        function("atan2", ATan2())
        function("lerp", Lerp())
        function("lerprotate", LerpRotate())
        function("random", Random())
        function("random_integer", RandomInteger())
        function("die_roll", DieRoll())
        function("die_roll_integer", DieRollInteger())
        function("hermite_blend", HermitBlend())
        function("min_angle", MinAngle())
        function("randomi", RandomInteger())
        function("roll", DieRoll())
        function("rolli", DieRollInteger())
        function("hermite", HermitBlend())
    }
}