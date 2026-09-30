package rip.ysm.compat.elytraslot.fabric

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack

object ElytraSlotCompatImpl {
    @JvmStatic
    fun isLoaded(): Boolean = FabricLoader.getInstance().isModLoaded("elytraslot")

    @JvmStatic
    fun getElytraItem(livingEntity: LivingEntity): ItemStack = ItemStack.EMPTY
}
