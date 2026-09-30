package rip.ysm.compat.parcool.fabric

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.world.entity.player.Player
import org.apache.commons.lang3.tuple.Pair
import java.util.Optional
import java.util.function.BiFunction

object ParcoolCompatImpl {
    @JvmStatic
    fun isLoaded(): Boolean = FabricLoader.getInstance().isModLoaded("parcool")

    @JvmStatic
    fun getInCompatibleInfo(): Optional<Pair<String, String>> = Optional.empty()

    @JvmStatic
    fun getControllerFactory(): Optional<BiFunction<String, CustomPlayerEntity, IAnimationController<CustomPlayerEntity>>> =
        Optional.empty()

    @JvmStatic
    fun isPlayerParcooling(player: Player): Boolean = false

    @JvmStatic
    fun getActionName(player: Player): String = ""

    @JvmStatic
    fun registerBindings(binding: CtrlBinding) {
    }
}
