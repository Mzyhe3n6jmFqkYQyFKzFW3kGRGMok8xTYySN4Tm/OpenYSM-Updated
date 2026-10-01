package rip.ysm.compat.parcool

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import net.minecraft.world.entity.player.Player
import org.apache.commons.lang3.tuple.Pair
import rip.ysm.compat.ModCompat
import rip.ysm.compat.parcool.fabric.ParcoolCompatImpl
import java.util.*
import java.util.function.BiFunction

object ParcoolCompat : ModCompat("parcool") {
    @JvmStatic
    fun getInCompatibleInfo(): Optional<Pair<String, String>> {
        if (!isModLoaded) return Optional.empty()
        return ParcoolCompatImpl.getInCompatibleInfo()
    }

    @JvmStatic
    fun getControllerFactory(): Optional<BiFunction<String, CustomPlayerEntity, IAnimationController<CustomPlayerEntity>>> {
        if (!isModLoaded) return Optional.empty()
        return ParcoolCompatImpl.getControllerFactory()
    }

    @JvmStatic
    fun isPlayerParcooling(player: Player): Boolean {
        return isModLoaded && ParcoolCompatImpl.isPlayerParcooling(player)
    }

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
