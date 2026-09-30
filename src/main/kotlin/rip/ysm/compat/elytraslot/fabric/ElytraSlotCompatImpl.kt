package rip.ysm.compat.elytraslot.fabric

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import rip.ysm.compat.elytraslot.ElytraSlotCompat

object ElytraSlotCompatImpl {
    @JvmStatic
    fun isLoaded(): Boolean = ElytraSlotCompat.isModLoaded

    @JvmStatic
    fun getElytraItem(livingEntity: LivingEntity): ItemStack = ItemStack.EMPTY
}
