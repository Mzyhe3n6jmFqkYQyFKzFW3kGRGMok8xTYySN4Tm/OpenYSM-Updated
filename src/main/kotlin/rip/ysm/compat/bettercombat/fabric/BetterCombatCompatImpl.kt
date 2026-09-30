package rip.ysm.compat.bettercombat.fabric

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import net.fabricmc.loader.api.FabricLoader

object BetterCombatCompatImpl {
    @JvmStatic
    fun isLoaded(): Boolean = FabricLoader.getInstance().isModLoaded("bettercombat")

    @JvmStatic
    fun registerBindings(binding: CtrlBinding) {
    }
}
