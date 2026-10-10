package rip.ysm.compat.cosmeticarmorreworked

import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import rip.ysm.compat.ModCompat
import rip.ysm.compat.cosmeticarmorreworked.fabric.CosmeticArmorHelperImpl

object CosmeticArmorHelper : ModCompat("cosmeticarmorreworked") {
    fun getArmorItem(entity: LivingEntity, slot: EquipmentSlot): ItemStack {
        if (!isModLoaded) return ItemStack.EMPTY
        return CosmeticArmorHelperImpl.getArmorItem(entity, slot)
    }

    fun getElytraItem(livingEntity: LivingEntity): ItemStack {
        if (!isModLoaded) return ItemStack.EMPTY
        return CosmeticArmorHelperImpl.getElytraItem(livingEntity)
    }
}
