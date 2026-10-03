package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.LivingEntityFunction
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function.ArgumentCollection
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentHelper
import rip.ysm.compat.cosmeticarmorreworked.CosmeticArmorHelper

class EquippedEnchantmentLevel : LivingEntityFunction() {
    override fun eval(context: ExecutionContext<IContext<LivingEntity>>, arguments: ArgumentCollection): Any? {
        val slotType: EquipmentSlot = MolangUtils.parseSlotType(context, arguments, 0) ?: return null
        val stack: ItemStack = CosmeticArmorHelper.getArmorItem(context.entity().entity(), slotType)
        if (stack.isEmpty) {
            return 0
        }
        var enchantmentLevel: Int = 0
        for (i in 1 until arguments.size()) {
            val id: Identifier? = arguments.getResourceLocation(context, i)
            if (id != null) {
                val holder: Holder<Enchantment>? = context.entity().entity().level().registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT)
                    .get(ResourceKey.create(Registries.ENCHANTMENT, id))
                    .orElse(null)
                if (holder != null) {
                    enchantmentLevel += EnchantmentHelper.getItemEnchantmentLevel(holder, stack)
                }
            }
        }
        return enchantmentLevel
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size >= 2
    }
}