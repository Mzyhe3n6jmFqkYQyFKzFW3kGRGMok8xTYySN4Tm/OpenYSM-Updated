package rip.ysm.compat.create.fabric

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import net.minecraft.world.entity.player.Player
import rip.ysm.compat.create.CreateCompat

object CreateCompatImpl {
    @JvmStatic
    fun isLoaded(): Boolean = CreateCompat.isModLoaded

    @JvmStatic
    fun isPlayerOnCreateContraption(player: Player): Boolean = false

    @JvmStatic
    fun registerCreateFunctions(binding: CtrlBinding) {
    }
}
