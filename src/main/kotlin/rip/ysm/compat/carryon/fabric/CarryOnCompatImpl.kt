package rip.ysm.compat.carryon.fabric

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import net.minecraft.world.entity.player.Player
import java.util.*
import java.util.function.BiFunction

object CarryOnCompatImpl {
    @JvmStatic
    fun getControllerFactory(): Optional<BiFunction<String, CustomPlayerEntity, IAnimationController<CustomPlayerEntity>>> {
        return Optional.empty()
    }

    @JvmStatic
    fun isPlayerCarrying(player: Player): Boolean = false

    @JvmStatic
    fun registerBindings(binding: CtrlBinding) {
        binding.livingEntityVar("carryon_type") { StringPool.EMPTY }
        binding.livingEntityVar("carryon_is_princess") { false }
    }
}
