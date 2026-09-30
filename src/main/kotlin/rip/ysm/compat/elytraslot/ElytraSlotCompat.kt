package rip.ysm.compat.elytraslot

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import rip.ysm.compat.elytraslot.fabric.ElytraSlotCompatImpl

object ElytraSlotCompat {
    @JvmStatic
    fun isLoaded(): Boolean = ElytraSlotCompatImpl.isLoaded()

    @JvmStatic
    fun getElytraItem(livingEntity: LivingEntity): ItemStack = ElytraSlotCompatImpl.getElytraItem(livingEntity)
}
