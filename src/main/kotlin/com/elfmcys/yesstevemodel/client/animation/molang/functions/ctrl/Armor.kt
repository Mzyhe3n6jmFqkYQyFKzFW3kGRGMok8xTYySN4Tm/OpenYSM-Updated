package com.elfmcys.yesstevemodel.client.animation.molang.functions.ctrl

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.LivingEntityFunction
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function.ArgumentCollection
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.LivingEntity

class Armor : LivingEntityFunction() {
    override fun eval(context: ExecutionContext<IContext<LivingEntity>>, arguments: ArgumentCollection): Any? {
        val slotType = MolangUtils.parseSlotType(context, arguments, 0)
        if (slotType == null || !slotType.isArmor) return null
        val id = arguments.getAsString(context, 1) ?: return 0
        val entity = context.entity.entity
        if (id.isBlank()) return 0
        val itemBySlot = entity.getItemBySlot(slotType)
        if (itemBySlot.isEmpty && id == EMPTY_ITEM) return 1
        val strSubstring = id.substring(1)
        if (id.startsWith(PREFIX_ITEM_ID)) {
            val key = BuiltInRegistries.ITEM.getKey(itemBySlot.item)
            return if (strSubstring == key.toString()) 1 else 0
        }
        if (id.startsWith(PREFIX_ITEM_TAG)) {
            val tag = TagKey.create(Registries.ITEM, Identifier.parse(strSubstring))
            return if (itemBySlot.`is`(tag)) 1 else 0
        }
        return 0
    }

    override fun validateArgumentSize(size: Int): Boolean = size == 2 || size == 3

    companion object {
        const val PREFIX_ITEM_ID = "$"
        const val PREFIX_ITEM_TAG = "#"
        const val EMPTY_ITEM = "empty"

        @JvmStatic
        fun create(): Armor = Armor()
    }
}