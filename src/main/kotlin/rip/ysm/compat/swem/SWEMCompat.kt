package rip.ysm.compat.swem

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import net.minecraft.world.entity.LivingEntity
import rip.ysm.compat.ModCompat
import rip.ysm.compat.swem.fabric.SWEMCompatImpl

object SWEMCompat : ModCompat("swem") {
    @JvmStatic
    fun isLoaded(): Boolean = isModLoaded

    @JvmStatic
    fun isRidingSWEM(livingEntity: LivingEntity): Boolean = SWEMCompatImpl.isRidingSWEM(livingEntity)

    @JvmStatic
    fun getHorseGaitName(livingEntity: LivingEntity): String = SWEMCompatImpl.getHorseGaitName(livingEntity)

    @JvmStatic
    fun registerControllerFunctions(binding: CtrlBinding) {
        SWEMCompatImpl.registerControllerFunctions(binding)
    }
}
