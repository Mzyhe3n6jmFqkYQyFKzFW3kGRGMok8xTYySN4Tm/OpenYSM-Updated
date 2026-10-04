package rip.ysm.compat.carryon.fabric

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import com.elfmcys.yesstevemodel.client.animation.predicate.PlayerAnimationPredicate
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.geckolib3.core.controller.CompositeAnimationController
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import net.minecraft.world.entity.player.Player
import rip.ysm.compat.carryon.CarryOnDataHelper
import java.util.*

// TODO: Replace Optional
object CarryOnCompatImpl {
    @JvmStatic
    fun getControllerFactory(): Optional<(String, CustomPlayerEntity) -> IAnimationController<CustomPlayerEntity>> =
        Optional.of { animationEntryKey, entity ->
            CompositeAnimationController(entity, animationEntryKey, 0.1f, PlayerAnimationPredicate())
        }

    @JvmStatic
    fun isPlayerCarrying(player: Player): Boolean = CarryOnDataHelper.isPlayerCarrying(player)

    @JvmStatic
    fun registerBindings(binding: CtrlBinding) {
        binding.livingEntityVar("carryon_type") {
            val entity = it.entity()
            if (entity is Player)
                when (CarryOnDataHelper.getCarryType(entity)) {
                    CarryOnDataHelper.CarryType.BLOCK -> "block"
                    CarryOnDataHelper.CarryType.ENTITY -> "entity"
                    CarryOnDataHelper.CarryType.PLAYER -> "player"
                    CarryOnDataHelper.CarryType.NONE -> StringPool.EMPTY
                } else StringPool.EMPTY
        }
        binding.livingEntityVar("carryon_is_princess") {
            val entity = it.entity()
            entity is Player && CarryOnDataHelper.isPrincess(entity)
        }
    }
}
