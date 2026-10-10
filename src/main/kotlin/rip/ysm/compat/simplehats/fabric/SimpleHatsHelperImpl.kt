package rip.ysm.compat.simplehats.fabric

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import rip.ysm.compat.ModCompat

object SimpleHatsHelperImpl : ModCompat("simplehats") {
    fun getHatItem(livingEntity: LivingEntity): ItemStack = ItemStack.EMPTY
}
