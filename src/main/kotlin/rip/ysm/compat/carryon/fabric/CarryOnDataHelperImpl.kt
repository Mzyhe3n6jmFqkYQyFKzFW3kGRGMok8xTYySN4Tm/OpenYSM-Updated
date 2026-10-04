package rip.ysm.compat.carryon.fabric

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import rip.ysm.compat.ModCompat
import rip.ysm.compat.carryon.CarryOnDataHelper.CarryType
import tschipp.carryon.common.carry.CarryOnData
import tschipp.carryon.common.carry.CarryOnDataManager

object CarryOnDataHelperImpl : ModCompat("carryon") {
    @JvmStatic
    fun isPlayerCarrying(livingEntity: LivingEntity): Boolean {
        if (livingEntity !is Player) return false
        val carryData = CarryOnDataManager.getCarryData(livingEntity)
        return carryData != null && carryData.isCarrying
    }

    @JvmStatic
    fun getCarryType(player: Player): CarryType {
        val carryData = CarryOnDataManager.getCarryData(player) ?: return CarryType.NONE
        return when (carryData.type) {
            CarryOnData.CarryType.BLOCK -> CarryType.BLOCK
            CarryOnData.CarryType.ENTITY -> CarryType.ENTITY
            CarryOnData.CarryType.PLAYER -> CarryType.PLAYER
            else -> CarryType.NONE
        }
    }

    @JvmStatic
    fun isPrincess(player: Player): Boolean {
        val vehicle = player.vehicle
        return vehicle is Player && getCarryType(vehicle) == CarryType.PLAYER
    }
}
