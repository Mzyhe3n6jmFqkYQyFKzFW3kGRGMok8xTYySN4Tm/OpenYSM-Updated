package rip.ysm.compat.cosmeticarmorreworked.fabric

import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack

object CosmeticArmorHelperImpl {
    @JvmStatic
    fun getArmorItem(entity: LivingEntity, slot: EquipmentSlot): ItemStack = ItemStack.EMPTY

    @JvmStatic
    fun getElytraItem(livingEntity: LivingEntity): ItemStack = ItemStack.EMPTY
}
