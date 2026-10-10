package rip.ysm.compat.playeranimator

import net.minecraft.client.player.AbstractClientPlayer
import rip.ysm.compat.ModCompat
import rip.ysm.compat.playeranimator.fabric.PlayerAnimatorCompatImpl

object PlayerAnimatorCompat : ModCompat("player-animation-lib") {
    fun isPlayerAnimated(abstractClientPlayer: AbstractClientPlayer): Boolean =
        isModLoaded && PlayerAnimatorCompatImpl.isPlayerAnimated(abstractClientPlayer)
}
