package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.query

import net.minecraft.core.registries.Registries
import rip.ysm.compat.cosmeticarmorreworked.CosmeticArmorHelper
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.LivingEntityFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack

open class IsItemNameAny : LivingEntityFunction() {
    open fun eval(context: ExecutionContext<IContext<LivingEntity>>, arguments: ArgumentCollection): Any {
        var key: Identifier? = null
        var slotType: EquipmentSlot = MolangUtils.parseSlotType(context, arguments, 0)
        if (slotType == null) {
            return null
        }
        var stack: ItemStack = CosmeticArmorHelper.getArmorItem(context.entity().entity(), slotType)
        if (!stack.isEmpty() && key = BuiltInRegistries.ITEM.getKey(stack.getItem()) != null) {
            var i = 1
            while (i < arguments.size()) {
                var location: Identifier = arguments.getResourceLocation(context, i)
                if (location == null) {
                    return null
                }
                if (location.equals(key)) {
                    return true
                }
                i++
            }
            return false
        }
        return false
    }
    open fun validateArgumentSize(size: Int): Boolean {
        return size >= 2
    }
}