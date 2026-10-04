package rip.ysm.compat.playeranimator.fabric

import net.minecraft.client.player.AbstractClientPlayer
import rip.ysm.compat.ModCompat

object PlayerAnimatorCompatImpl : ModCompat("player-animation-lib") {
    @JvmStatic
    fun isPlayerAnimated(abstractClientPlayer: AbstractClientPlayer): Boolean = false
}
