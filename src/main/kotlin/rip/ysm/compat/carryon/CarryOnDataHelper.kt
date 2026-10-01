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

    @JvmStatic
    fun isPlayerCarrying(livingEntity: LivingEntity): Boolean {
        return isModLoaded && CarryOnDataHelperImpl.isPlayerCarrying(livingEntity)
    }

    @JvmStatic
    fun getCarryType(player: Player): CarryType {
        if (!isModLoaded) return CarryType.NONE
        return CarryOnDataHelperImpl.getCarryType(player)
    }

    @JvmStatic
    fun isPrincess(player: Player): Boolean = isModLoaded && CarryOnDataHelperImpl.isPrincess(player)
}
