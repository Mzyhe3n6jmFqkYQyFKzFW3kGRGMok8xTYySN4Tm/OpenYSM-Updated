package rip.ysm.compat.bettercombat

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import rip.ysm.compat.ModCompat
import rip.ysm.compat.bettercombat.fabric.BetterCombatCompatImpl

object BetterCombatCompat : ModCompat("bettercombat") {
    @JvmStatic
    fun registerBindings(binding: CtrlBinding) {
        if (!isModLoaded) return
        BetterCombatCompatImpl.registerBindings(binding)
    }
}
