package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.query

import rip.ysm.compat.cosmeticarmorreworked.CosmeticArmorHelper
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.LivingEntityFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack

open class RemainingDurability : LivingEntityFunction() {
    open fun eval(context: ExecutionContext<IContext<LivingEntity>>, arguments: ArgumentCollection): Any {
        var stack: ItemStack = CosmeticArmorHelper.getArmorItem(context.entity().entity(), MolangUtils.parseSlotType(context, arguments, 0))
        return stack.getMaxDamage() - stack.getDamageValue()
    }
    open fun validateArgumentSize(size: Int): Boolean {
        return size == 1
    }
}