@file:Suppress("unused")

package rip.ysm.compat.parcool

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import net.minecraft.world.entity.player.Player
import org.apache.commons.lang3.tuple.Pair
import rip.ysm.compat.ModCompat
import rip.ysm.compat.parcool.fabric.ParcoolCompatImpl

object ParcoolCompat : ModCompat("parcool") {
    @JvmStatic
    val inCompatibleInfo: Pair<String, String>?
        get() {
            if (!isModLoaded) return null
            return ParcoolCompatImpl.inCompatibleInfo
        }

    @JvmStatic
    val controllerFactory: ((String, CustomPlayerEntity) -> IAnimationController<CustomPlayerEntity>)?
        get() {
            if (!isModLoaded) return null
            return ParcoolCompatImpl.controllerFactory
        }

    @JvmStatic
    fun isPlayerParcooling(player: Player): Boolean = isModLoaded && ParcoolCompatImpl.isPlayerParcooling(player)

    @JvmStatic
    fun getActionName(player: Player): String {
        if (!isModLoaded) return ""
        return ParcoolCompatImpl.getActionName(player)
    }

    @JvmStatic
    fun registerBindings(binding: CtrlBinding) {
        if (!isModLoaded) return
        ParcoolCompatImpl.registerBindings(binding)
    }
}
