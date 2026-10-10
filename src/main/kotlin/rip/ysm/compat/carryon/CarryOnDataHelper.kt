package rip.ysm.compat.carryon

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import rip.ysm.compat.ModCompat
import rip.ysm.compat.carryon.fabric.CarryOnDataHelperImpl

object CarryOnDataHelper : ModCompat("carryon") {
    enum class CarryType {
        NONE,
        BLOCK,
        ENTITY,
        PLAYER
    }

    fun isPlayerCarrying(livingEntity: LivingEntity): Boolean =
        isModLoaded && CarryOnDataHelperImpl.isPlayerCarrying(livingEntity)

    fun getCarryType(player: Player): CarryType {
        if (!isModLoaded) return CarryType.NONE
        return CarryOnDataHelperImpl.getCarryType(player)
    }

    fun isPrincess(player: Player): Boolean = isModLoaded && CarryOnDataHelperImpl.isPrincess(player)
}
