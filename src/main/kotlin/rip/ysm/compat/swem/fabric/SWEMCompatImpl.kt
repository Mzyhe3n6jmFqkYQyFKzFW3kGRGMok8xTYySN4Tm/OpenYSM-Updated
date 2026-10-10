package rip.ysm.compat.swem.fabric

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import net.minecraft.world.entity.LivingEntity

object SWEMCompatImpl {
    fun isRidingSWEM(livingEntity: LivingEntity): Boolean = false

    fun getHorseGaitName(livingEntity: LivingEntity): String = ""

    fun registerControllerFunctions(binding: CtrlBinding) {
    }
}
