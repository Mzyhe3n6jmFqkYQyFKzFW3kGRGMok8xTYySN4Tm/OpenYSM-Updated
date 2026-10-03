package com.elfmcys.yesstevemodel.client.animation.condition

class ArmorConditions {
    val conditionArmor: ConditionArmor = ConditionArmor()

    fun addCondition(str: String) {
        conditionArmor.addTest(str)
    }

    fun getConditionArmor(): ConditionArmor = conditionArmor
}