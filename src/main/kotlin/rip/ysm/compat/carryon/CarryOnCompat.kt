package rip.ysm.compat.carryon

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import net.minecraft.world.entity.player.Player
import rip.ysm.compat.ModCompat
import rip.ysm.compat.carryon.fabric.CarryOnCompatImpl

// TODO: Funny anim when player is carrying and crawl at the same time
object CarryOnCompat : ModCompat("carryon") {
    @JvmStatic
    fun getControllerFactory(): ((String, CustomPlayerEntity) -> IAnimationController<CustomPlayerEntity>)? {
        if (!isModLoaded) return null
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
