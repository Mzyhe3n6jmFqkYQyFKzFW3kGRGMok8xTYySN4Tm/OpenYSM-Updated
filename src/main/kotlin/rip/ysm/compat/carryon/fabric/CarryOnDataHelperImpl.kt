package rip.ysm.compat.carryon.fabric

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import rip.ysm.compat.carryon.CarryOnDataHelper.CarryType
import tschipp.carryon.common.carry.CarryOnDataManager

object CarryOnDataHelperImpl {
    @JvmStatic
    fun isPlayerCarrying(livingEntity: LivingEntity): Boolean =
        livingEntity is Player && getCarryType(livingEntity) != CarryType.NONE

    @JvmStatic
    fun getCarryType(player: Player): CarryType {
        val carryData = CarryOnDataManager.getCarryData(player)
        if (!carryData.isCarrying) return CarryType.NONE
        val nbt = carryData.nbt
        if (nbt != null) {
            val typeStr = nbt.getStringOr("type", "").uppercase()
            return when (typeStr) {
                "BLOCK" -> CarryType.BLOCK
                "ENTITY" -> CarryType.ENTITY
                "PLAYER" -> CarryType.PLAYER
                else -> CarryType.NONE
            }
        }

        val str = carryData.toString().uppercase()
        return when {
            str.contains("BLOCK") -> CarryType.BLOCK
            str.contains("ENTITY") -> CarryType.ENTITY
            str.contains("PLAYER") -> CarryType.PLAYER
            else -> CarryType.NONE
        }
    }

    @JvmStatic
    fun isPrincess(player: Player): Boolean {
        val vehicle = player.vehicle
        return vehicle is Player && getCarryType(vehicle) == CarryType.PLAYER
    }
}
