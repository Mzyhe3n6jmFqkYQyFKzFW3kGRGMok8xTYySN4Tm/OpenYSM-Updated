@file:Suppress("unused")

package rip.ysm.compat.swem

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import net.minecraft.world.entity.LivingEntity
import rip.ysm.compat.ModCompat
import rip.ysm.compat.swem.fabric.SWEMCompatImpl

object SWEMCompat : ModCompat("swem") {
    @JvmStatic
    fun isRidingSWEM(livingEntity: LivingEntity): Boolean = isModLoaded && SWEMCompatImpl.isRidingSWEM(livingEntity)

    @JvmStatic
    fun getHorseGaitName(livingEntity: LivingEntity): String {
        if (!isModLoaded) return ""
        return SWEMCompatImpl.getHorseGaitName(livingEntity)
    }

    @JvmStatic
    fun registerControllerFunctions(binding: CtrlBinding) {
        if (!isModLoaded) return
        SWEMCompatImpl.registerControllerFunctions(binding)
    }
}
