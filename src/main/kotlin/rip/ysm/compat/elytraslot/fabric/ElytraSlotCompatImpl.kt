package rip.ysm.compat.elytraslot.fabric

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack

object ElytraSlotCompatImpl {
    @JvmStatic
    fun getElytraItem(livingEntity: LivingEntity): ItemStack = ItemStack.EMPTY
}
