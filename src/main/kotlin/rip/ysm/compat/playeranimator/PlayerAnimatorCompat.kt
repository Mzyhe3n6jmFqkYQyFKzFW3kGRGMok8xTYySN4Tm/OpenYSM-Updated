package rip.ysm.compat.playeranimator

import net.minecraft.client.player.AbstractClientPlayer
import rip.ysm.compat.playeranimator.fabric.PlayerAnimatorCompatImpl

object PlayerAnimatorCompat {
    @JvmStatic
    fun isLoaded(): Boolean = PlayerAnimatorCompatImpl.isLoaded()

    @JvmStatic
    fun isPlayerAnimated(abstractClientPlayer: AbstractClientPlayer): Boolean =
        PlayerAnimatorCompatImpl.isPlayerAnimated(abstractClientPlayer)
}
