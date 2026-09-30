package rip.ysm.compat.swem.fabric

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.world.entity.LivingEntity

object SWEMCompatImpl {
    @JvmStatic
    fun isLoaded(): Boolean = FabricLoader.getInstance().isModLoaded("swem")

    @JvmStatic
    fun isRidingSWEM(livingEntity: LivingEntity): Boolean = false

    @JvmStatic
    fun getHorseGaitName(livingEntity: LivingEntity): String = ""

    @JvmStatic
    fun registerControllerFunctions(binding: CtrlBinding) {
    }
}
