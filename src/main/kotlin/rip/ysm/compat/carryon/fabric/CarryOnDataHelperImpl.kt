package rip.ysm.compat.carryon.fabric

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import rip.ysm.compat.carryon.CarryOnDataHelper.CarryType

object CarryOnDataHelperImpl {
    @JvmStatic
    fun isPlayerCarrying(livingEntity: LivingEntity): Boolean = false

    @JvmStatic
    fun getCarryType(player: Player): CarryType = CarryType.NONE
}
