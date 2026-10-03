package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.query

import rip.ysm.compat.cosmeticarmorreworked.CosmeticArmorHelper
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.LivingEntityFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import net.minecraft.world.entity.LivingEntity

open class MaxDurability : LivingEntityFunction() {
    open fun eval(context: ExecutionContext<IContext<LivingEntity>>, arguments: ArgumentCollection): Any {
        return CosmeticArmorHelper.getArmorItem(context.entity().entity(), MolangUtils.parseSlotType(context, arguments, 0)).getMaxDamage()
    }
    open fun validateArgumentSize(size: Int): Boolean {
        return size == 1
    }
}