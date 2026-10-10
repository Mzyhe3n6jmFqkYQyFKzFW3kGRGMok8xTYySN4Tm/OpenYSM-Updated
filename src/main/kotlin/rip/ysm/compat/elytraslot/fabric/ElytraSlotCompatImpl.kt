package rip.ysm.compat.elytraslot.fabric

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import rip.ysm.compat.ModCompat

// TODO: Implement
object ElytraSlotCompatImpl : ModCompat("elytraslot") {
    fun getElytraItem(livingEntity: LivingEntity): ItemStack = ItemStack.EMPTY
}
