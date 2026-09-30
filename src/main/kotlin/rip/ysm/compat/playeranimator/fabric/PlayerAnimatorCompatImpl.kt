package rip.ysm.compat.playeranimator.fabric

import net.minecraft.client.player.AbstractClientPlayer
import rip.ysm.compat.playeranimator.PlayerAnimatorCompat

object PlayerAnimatorCompatImpl {
    @JvmStatic
    fun isLoaded(): Boolean = PlayerAnimatorCompat.isModLoaded

    @JvmStatic
    fun isPlayerAnimated(abstractClientPlayer: AbstractClientPlayer): Boolean = false
}
