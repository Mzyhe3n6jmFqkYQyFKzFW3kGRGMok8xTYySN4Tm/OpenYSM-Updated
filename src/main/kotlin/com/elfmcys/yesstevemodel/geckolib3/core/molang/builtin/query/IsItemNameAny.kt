package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.query

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.LivingEntityFunction
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import rip.ysm.compat.cosmeticarmorreworked.CosmeticArmorHelper

class IsItemNameAny : LivingEntityFunction() {
    override fun eval(context: ExecutionContext<IContext<LivingEntity>>, arguments: Function.ArgumentCollection): Any? {
        val slotType: EquipmentSlot = MolangUtils.parseSlotType(context, arguments, 0) ?: return null
        val stack: ItemStack = CosmeticArmorHelper.getArmorItem(context.entity().entity(), slotType)
        if (stack.isEmpty) {
            return false
        }
        val key: Identifier = BuiltInRegistries.ITEM.getKey(stack.item)
        for (i in 1 until arguments.size()) {
            val location: Identifier = arguments.getResourceLocation(context, i) ?: return null
            if (location == key) {
                return true
            }
        }
        return false
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size >= 2
    }
}