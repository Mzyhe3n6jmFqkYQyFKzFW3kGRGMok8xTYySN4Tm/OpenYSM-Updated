package rip.ysm.compat.playeranimator.fabric

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.player.AbstractClientPlayer

object PlayerAnimatorCompatImpl {
    @JvmStatic
    fun isLoaded(): Boolean = FabricLoader.getInstance().isModLoaded("player-animation-lib")

    @JvmStatic
    fun isPlayerAnimated(abstractClientPlayer: AbstractClientPlayer): Boolean = false
}
