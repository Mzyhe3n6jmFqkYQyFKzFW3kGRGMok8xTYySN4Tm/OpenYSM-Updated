package rip.ysm.compat.bettercombat

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import rip.ysm.compat.ModCompat
import rip.ysm.compat.bettercombat.fabric.BetterCombatCompatImpl

object BetterCombatCompat : ModCompat("bettercombat") {
    @JvmStatic
    fun isLoaded(): Boolean = isModLoaded

    @JvmStatic
    fun registerBindings(binding: CtrlBinding) {
        BetterCombatCompatImpl.registerBindings(binding)
    }
}
