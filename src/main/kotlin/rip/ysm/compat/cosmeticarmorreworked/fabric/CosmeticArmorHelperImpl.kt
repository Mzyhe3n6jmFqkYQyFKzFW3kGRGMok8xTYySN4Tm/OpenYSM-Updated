package rip.ysm.compat.cosmeticarmorreworked.fabric

import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import rip.ysm.compat.cosmeticarmorreworked.CosmeticArmorHelper

object CosmeticArmorHelperImpl {
    @JvmStatic
    fun isLoaded(): Boolean = CosmeticArmorHelper.isModLoaded

    @JvmStatic
    fun getArmorItem(entity: LivingEntity, slot: EquipmentSlot): ItemStack = entity.getItemBySlot(slot)

    @JvmStatic
    fun getElytraItem(livingEntity: LivingEntity): ItemStack {
        val chest = livingEntity.getItemBySlot(EquipmentSlot.CHEST)
        return if (chest.has(DataComponents.GLIDER)) chest else ItemStack.EMPTY
    }
}
