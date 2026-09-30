package rip.ysm.compat.create.fabric

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.world.entity.player.Player

object CreateCompatImpl {
    @JvmStatic
    fun isLoaded(): Boolean = FabricLoader.getInstance().isModLoaded("create")

    @JvmStatic
    fun isPlayerOnCreateContraption(player: Player): Boolean = false

    @JvmStatic
    fun registerCreateFunctions(binding: CtrlBinding) {
    }
}
