package rip.ysm.compat.simplehats.fabric

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import rip.ysm.compat.simplehats.SimpleHatsHelper

object SimpleHatsHelperImpl {
    @JvmStatic
    fun isLoaded(): Boolean = SimpleHatsHelper.isModLoaded

    @JvmStatic
    fun getHatItem(livingEntity: LivingEntity): ItemStack = ItemStack.EMPTY
}
