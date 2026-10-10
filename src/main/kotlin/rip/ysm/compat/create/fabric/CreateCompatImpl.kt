package rip.ysm.compat.create.fabric

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import net.minecraft.world.entity.player.Player
import rip.ysm.compat.ModCompat

object CreateCompatImpl : ModCompat("create") {
    fun isPlayerOnCreateContraption(player: Player): Boolean = false

    fun registerCreateFunctions(binding: CtrlBinding) {
    }
}
