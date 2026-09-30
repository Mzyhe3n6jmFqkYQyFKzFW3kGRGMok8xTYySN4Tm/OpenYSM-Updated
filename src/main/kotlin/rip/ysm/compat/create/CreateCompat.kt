package rip.ysm.compat.create

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import net.minecraft.world.entity.player.Player
import rip.ysm.compat.ModCompat
import rip.ysm.compat.create.fabric.CreateCompatImpl

object CreateCompat : ModCompat("create") {
    @JvmStatic
    fun isLoaded(): Boolean = isModLoaded

    @JvmStatic
    fun isPlayerOnCreateContraption(player: Player): Boolean = CreateCompatImpl.isPlayerOnCreateContraption(player)

    @JvmStatic
    fun registerCreateFunctions(binding: CtrlBinding) {
        CreateCompatImpl.registerCreateFunctions(binding)
    }
}
