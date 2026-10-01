package rip.ysm.compat.create.fabric

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import net.minecraft.world.entity.player.Player

object CreateCompatImpl {
    @JvmStatic
    fun isPlayerOnCreateContraption(player: Player): Boolean = false

    @JvmStatic
    fun registerCreateFunctions(binding: CtrlBinding) {
    }
}
