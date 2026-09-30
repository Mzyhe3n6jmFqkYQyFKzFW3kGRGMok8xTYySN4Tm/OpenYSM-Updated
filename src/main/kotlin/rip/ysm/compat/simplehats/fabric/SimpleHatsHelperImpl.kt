package rip.ysm.compat.simplehats.fabric

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack

object SimpleHatsHelperImpl {
    @JvmStatic
    fun isLoaded(): Boolean = FabricLoader.getInstance().isModLoaded("simplehats")

    @JvmStatic
    fun getHatItem(livingEntity: LivingEntity): ItemStack = ItemStack.EMPTY
}
