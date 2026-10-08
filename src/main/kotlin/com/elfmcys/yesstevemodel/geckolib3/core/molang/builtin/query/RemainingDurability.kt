package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.query

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.LivingEntityFunction
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import rip.ysm.compat.cosmeticarmorreworked.CosmeticArmorHelper

class RemainingDurability : LivingEntityFunction() {
    override fun eval(context: ExecutionContext<IContext<LivingEntity>>, arguments: Function.ArgumentCollection): Any? {
        val slotType: EquipmentSlot = MolangUtils.parseSlotType(context, arguments, 0) ?: return null
        val stack: ItemStack = CosmeticArmorHelper.getArmorItem(context.entity.entity, slotType)
        return stack.maxDamage - stack.damageValue
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size == 1
    }
}