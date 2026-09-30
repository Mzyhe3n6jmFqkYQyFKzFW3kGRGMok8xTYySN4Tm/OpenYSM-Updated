package rip.ysm.compat.carryon

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import net.minecraft.world.entity.player.Player
import rip.ysm.compat.carryon.fabric.CarryOnCompatImpl
import java.util.Optional
import java.util.function.BiFunction

object CarryOnCompat {
    @JvmStatic
    fun isLoaded(): Boolean = CarryOnCompatImpl.isLoaded()

    @JvmStatic
    fun getControllerFactory(): Optional<BiFunction<String, CustomPlayerEntity, IAnimationController<CustomPlayerEntity>>> =
        CarryOnCompatImpl.getControllerFactory()

    @JvmStatic
    fun isPlayerCarrying(player: Player): Boolean = CarryOnCompatImpl.isPlayerCarrying(player)

    @JvmStatic
    fun registerBindings(binding: CtrlBinding) {
        CarryOnCompatImpl.registerBindings(binding)
    }
}
