package rip.ysm.compat.parcool.fabric

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import net.minecraft.world.entity.player.Player
import org.apache.commons.lang3.tuple.Pair
import rip.ysm.compat.ModCompat

object ParcoolCompatImpl : ModCompat("parcool") {
    @JvmStatic
    fun getInCompatibleInfo(): Pair<String, String>? = null

    @JvmStatic
    fun getControllerFactory(): ((String, CustomPlayerEntity) -> IAnimationController<CustomPlayerEntity>)? = null

    @JvmStatic
    fun isPlayerParcooling(player: Player): Boolean = false

    @JvmStatic
    fun getActionName(player: Player): String = ""

    @JvmStatic
    fun registerBindings(binding: CtrlBinding) {
    }
}
