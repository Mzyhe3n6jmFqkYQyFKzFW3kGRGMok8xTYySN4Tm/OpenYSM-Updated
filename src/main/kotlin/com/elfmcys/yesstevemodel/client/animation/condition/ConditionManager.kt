package com.elfmcys.yesstevemodel.client.animation.condition

import net.minecraft.world.InteractionHand
import rip.ysm.compat.gun.tacz.ConditionTAC

class ConditionManager {
    val swingMainhand: ConditionSwing = ConditionSwing(InteractionHand.MAIN_HAND)
    val swingOffhand: ConditionSwing = ConditionSwing(InteractionHand.OFF_HAND)
    val useMainhand: ConditionUse = ConditionUse(InteractionHand.MAIN_HAND)
    val useOffhand: ConditionUse = ConditionUse(InteractionHand.OFF_HAND)
    val holdMainhand: ConditionHold = ConditionHold(InteractionHand.MAIN_HAND)
    val holdOffhand: ConditionHold = ConditionHold(InteractionHand.OFF_HAND)
    val armor: ConditionArmor = ConditionArmor()
    val tac: ConditionTAC = ConditionTAC()
    val vehicle: ConditionVehicle = ConditionVehicle()
    val passenger: ConditionPassenger = ConditionPassenger()
    val chair: ConditionChair = ConditionChair()

    fun addTest(name: String) {
        swingMainhand.addTest(name)
        swingOffhand.addTest(name)
        useMainhand.addTest(name)
        useOffhand.addTest(name)
        holdMainhand.addTest(name)
        holdOffhand.addTest(name)
        armor.addTest(name)
        tac.addTest(name)
        vehicle.addTest(name)
        passenger.doTest(name)
        chair.addTest(name)
    }

    fun getVehicle(): ConditionVehicle = vehicle
    fun getChair(): ConditionChair = chair
}