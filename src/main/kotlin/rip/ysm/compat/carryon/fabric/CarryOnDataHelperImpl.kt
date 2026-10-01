package rip.ysm.compat.carryon.fabric

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import rip.ysm.compat.carryon.CarryOnDataHelper.CarryType
import tschipp.carryon.common.carry.CarryOnData
import tschipp.carryon.common.carry.CarryOnDataManager

object CarryOnDataHelperImpl {
    @JvmStatic
    fun isPlayerCarrying(livingEntity: LivingEntity): Boolean {
        if (livingEntity !is Player) return false
        return try {
            val carryData = CarryOnDataManager.getCarryData(livingEntity)
            carryData != null && carryData.isCarrying
        } catch (_: Throwable) {
            false
        }
    }

    @JvmStatic
    fun getCarryType(player: Player): CarryType {
        return try {
            val carryData = CarryOnDataManager.getCarryData(player) ?: return CarryType.NONE
            when (carryData.type) {
                CarryOnData.CarryType.BLOCK -> CarryType.BLOCK
                CarryOnData.CarryType.ENTITY -> CarryType.ENTITY
                CarryOnData.CarryType.PLAYER -> CarryType.PLAYER
                else -> CarryType.NONE
            }
        } catch (_: Throwable) {
            CarryType.NONE
        }
    }

    @JvmStatic
    fun isPrincess(player: Player): Boolean {
        val vehicle = player.vehicle
        return vehicle is Player && getCarryType(vehicle) == CarryType.PLAYER
    }
}
