package rip.ysm.compat.elytraslot

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import rip.ysm.compat.ModCompat
import rip.ysm.compat.elytraslot.fabric.ElytraSlotCompatImpl

object ElytraSlotCompat : ModCompat("elytraslot") {
    @JvmStatic
    fun isLoaded(): Boolean = isModLoaded

    @JvmStatic
    fun getElytraItem(livingEntity: LivingEntity): ItemStack = ElytraSlotCompatImpl.getElytraItem(livingEntity)
}
