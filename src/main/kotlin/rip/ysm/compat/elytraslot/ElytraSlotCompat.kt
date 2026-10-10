package rip.ysm.compat.elytraslot

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import rip.ysm.compat.ModCompat
import rip.ysm.compat.elytraslot.fabric.ElytraSlotCompatImpl

// TODO: Implement
object ElytraSlotCompat : ModCompat("elytraslot") {
    fun getElytraItem(livingEntity: LivingEntity): ItemStack {
        if (!isModLoaded) return ItemStack.EMPTY
        return ElytraSlotCompatImpl.getElytraItem(livingEntity)
    }
}
