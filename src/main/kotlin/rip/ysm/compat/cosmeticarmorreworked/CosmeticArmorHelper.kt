package rip.ysm.compat.cosmeticarmorreworked

import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import rip.ysm.compat.ModCompat
import rip.ysm.compat.cosmeticarmorreworked.fabric.CosmeticArmorHelperImpl

object CosmeticArmorHelper : ModCompat("cosmeticarmorreworked") {
    @JvmStatic
    fun isLoaded(): Boolean = isModLoaded

    @JvmStatic
    fun getArmorItem(entity: LivingEntity, slot: EquipmentSlot): ItemStack =
        CosmeticArmorHelperImpl.getArmorItem(entity, slot)

    @JvmStatic
    fun getElytraItem(livingEntity: LivingEntity): ItemStack =
        CosmeticArmorHelperImpl.getElytraItem(livingEntity)
}
