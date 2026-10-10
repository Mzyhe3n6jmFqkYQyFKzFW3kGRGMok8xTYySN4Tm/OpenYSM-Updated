package rip.ysm.compat.parcool.fabric

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import net.minecraft.world.entity.player.Player
import org.apache.commons.lang3.tuple.Pair
import rip.ysm.compat.ModCompat

object ParcoolCompatImpl : ModCompat("parcool") {
    val inCompatibleInfo: Pair<String, String>?
        get() = null

    val controllerFactory: ((String, CustomPlayerEntity) -> IAnimationController<CustomPlayerEntity>)?
        get() = null

    fun isPlayerParcooling(player: Player): Boolean = false

    fun getActionName(player: Player): String = ""

    fun registerBindings(binding: CtrlBinding) {
    }
}
