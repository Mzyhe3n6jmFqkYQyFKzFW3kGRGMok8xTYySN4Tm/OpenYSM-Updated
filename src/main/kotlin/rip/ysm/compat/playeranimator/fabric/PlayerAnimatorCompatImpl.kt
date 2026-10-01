package rip.ysm.compat.playeranimator.fabric

import net.minecraft.client.player.AbstractClientPlayer

object PlayerAnimatorCompatImpl {
    @JvmStatic
    fun isPlayerAnimated(abstractClientPlayer: AbstractClientPlayer): Boolean = false
}
