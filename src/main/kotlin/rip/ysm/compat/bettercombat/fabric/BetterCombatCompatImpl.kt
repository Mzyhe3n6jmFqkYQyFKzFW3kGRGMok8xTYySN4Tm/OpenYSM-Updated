package rip.ysm.compat.bettercombat.fabric

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import rip.ysm.compat.bettercombat.BetterCombatCompat

object BetterCombatCompatImpl {
    @JvmStatic
    fun isLoaded(): Boolean = BetterCombatCompat.isModLoaded

    @JvmStatic
    fun registerBindings(binding: CtrlBinding) {
    }
}
