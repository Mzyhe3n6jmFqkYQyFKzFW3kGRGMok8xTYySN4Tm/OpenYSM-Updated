package rip.ysm.compat.simplehats

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import rip.ysm.compat.ModCompat
import rip.ysm.compat.simplehats.fabric.SimpleHatsHelperImpl

object SimpleHatsHelper : ModCompat("simplehats") {
    @JvmStatic
    fun getHatItem(livingEntity: LivingEntity): ItemStack {
        if (!isModLoaded) return ItemStack.EMPTY
        return SimpleHatsHelperImpl.getHatItem(livingEntity)
    }
}
