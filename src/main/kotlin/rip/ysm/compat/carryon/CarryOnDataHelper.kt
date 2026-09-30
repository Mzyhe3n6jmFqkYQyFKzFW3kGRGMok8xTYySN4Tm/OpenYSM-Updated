package rip.ysm.compat.carryon

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import rip.ysm.compat.carryon.fabric.CarryOnDataHelperImpl

object CarryOnDataHelper {
    enum class CarryType {
        NONE,
        BLOCK,
        ENTITY,
        PLAYER
    }

    @JvmStatic
    fun isPlayerCarrying(livingEntity: LivingEntity): Boolean = CarryOnDataHelperImpl.isPlayerCarrying(livingEntity)

    @JvmStatic
    fun getCarryType(player: Player): CarryType = CarryOnDataHelperImpl.getCarryType(player)
}
