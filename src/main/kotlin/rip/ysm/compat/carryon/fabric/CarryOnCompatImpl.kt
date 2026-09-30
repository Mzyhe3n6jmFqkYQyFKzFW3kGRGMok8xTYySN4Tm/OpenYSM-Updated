package rip.ysm.compat.carryon.fabric

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import net.minecraft.world.entity.player.Player
import rip.ysm.compat.carryon.CarryOnCompat
import java.util.Optional
import java.util.function.BiFunction

object CarryOnCompatImpl {
    @JvmStatic
    fun isLoaded(): Boolean = CarryOnCompat.isModLoaded

    @JvmStatic
    fun getControllerFactory(): Optional<BiFunction<String, CustomPlayerEntity, IAnimationController<CustomPlayerEntity>>> =
        Optional.empty()

    @JvmStatic
    fun isPlayerCarrying(player: Player): Boolean = false

    @JvmStatic
    fun registerBindings(binding: CtrlBinding) {
    }
}
