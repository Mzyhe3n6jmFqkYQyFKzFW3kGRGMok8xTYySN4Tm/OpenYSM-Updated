package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.query

import net.minecraft.tags.TagKey
import net.minecraft.core.registries.Registries
import rip.ysm.compat.cosmeticarmorreworked.CosmeticArmorHelper
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.LivingEntityFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack

open class EquipmentItemAnyTag : LivingEntityFunction() {
    open fun eval(context: ExecutionContext<IContext<LivingEntity>>, arguments: ArgumentCollection): Any {
        var slotType: EquipmentSlot = MolangUtils.parseSlotType(context, arguments, 0)
        if (slotType == null) {
            return null
        }
        var stack: ItemStack = CosmeticArmorHelper.getArmorItem(context.entity().entity(), slotType)
        if (stack.isEmpty()) {
            return false
        }
        var i = 1
        while (i < arguments.size()) {
            var key: Identifier = arguments.getResourceLocation(context, i)
            if (key == null) {
                return null
            }
            if (stack.`is`(TagKey.create(Registries.ITEM, key))) {
                return true
            }
            i++
        }
        return false
    }
    open fun validateArgumentSize(size: Int): Boolean {
        return size >= 2
    }
}