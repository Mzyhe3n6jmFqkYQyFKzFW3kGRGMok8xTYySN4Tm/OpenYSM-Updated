package rip.ysm.compat.simplehats

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import rip.ysm.compat.simplehats.fabric.SimpleHatsHelperImpl

object SimpleHatsHelper {
    @JvmStatic
    fun isLoaded(): Boolean = SimpleHatsHelperImpl.isLoaded()

    @JvmStatic
    fun getHatItem(livingEntity: LivingEntity): ItemStack = SimpleHatsHelperImpl.getHatItem(livingEntity)
}
