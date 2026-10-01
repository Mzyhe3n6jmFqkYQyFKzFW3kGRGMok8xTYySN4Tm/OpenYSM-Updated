package rip.ysm.compat.simplehats.fabric

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack

object SimpleHatsHelperImpl {
    @JvmStatic
    fun getHatItem(livingEntity: LivingEntity): ItemStack = ItemStack.EMPTY
}
