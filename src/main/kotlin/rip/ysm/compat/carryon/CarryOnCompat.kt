package rip.ysm.compat.carryon

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import net.minecraft.world.entity.player.Player
import rip.ysm.compat.ModCompat
import rip.ysm.compat.carryon.fabric.CarryOnCompatImpl
import java.util.*
import java.util.function.BiFunction

object CarryOnCompat : ModCompat("carryon") {
    @JvmStatic
    fun isLoaded(): Boolean = isModLoaded

    @JvmStatic
    fun getControllerFactory(): Optional<BiFunction<String, CustomPlayerEntity, IAnimationController<CustomPlayerEntity>>> {
        if (!isModLoaded) return Optional.empty()
        return CarryOnCompatImpl.getControllerFactory()
    }

    @JvmStatic
    fun isPlayerCarrying(player: Player): Boolean = isModLoaded && CarryOnCompatImpl.isPlayerCarrying(player)

    @JvmStatic
    fun registerBindings(binding: CtrlBinding) {
        if (!isModLoaded) return
        CarryOnCompatImpl.registerBindings(binding)
    }
}
