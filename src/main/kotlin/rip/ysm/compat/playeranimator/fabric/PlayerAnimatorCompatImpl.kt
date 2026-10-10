package rip.ysm.compat.playeranimator.fabric

import net.minecraft.client.player.AbstractClientPlayer
import rip.ysm.compat.ModCompat

// TODO: Implement
object PlayerAnimatorCompatImpl : ModCompat("player-animation-lib") {
    fun isPlayerAnimated(abstractClientPlayer: AbstractClientPlayer): Boolean = false
}
