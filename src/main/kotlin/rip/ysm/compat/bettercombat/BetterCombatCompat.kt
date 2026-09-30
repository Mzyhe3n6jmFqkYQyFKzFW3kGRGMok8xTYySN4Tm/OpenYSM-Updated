package rip.ysm.compat.bettercombat

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import rip.ysm.compat.bettercombat.fabric.BetterCombatCompatImpl

object BetterCombatCompat {
    @JvmStatic
    fun isLoaded(): Boolean = BetterCombatCompatImpl.isLoaded()

    @JvmStatic
    fun registerBindings(binding: CtrlBinding) {
        BetterCombatCompatImpl.registerBindings(binding)
    }
}
