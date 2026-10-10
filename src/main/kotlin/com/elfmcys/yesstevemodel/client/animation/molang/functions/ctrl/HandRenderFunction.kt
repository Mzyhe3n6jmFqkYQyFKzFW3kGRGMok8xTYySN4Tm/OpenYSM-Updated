package com.elfmcys.yesstevemodel.client.animation.molang.functions.ctrl

import com.elfmcys.yesstevemodel.client.animation.condition.InnerClassify
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.LivingEntityFunction
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function.ArgumentCollection
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.tags.TagKey
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import java.util.*

class HandRenderFunction(private val handItemPredicate: HandItemPredicate) : LivingEntityFunction() {
    override fun eval(context: ExecutionContext<IContext<LivingEntity>>, arguments: ArgumentCollection): Any {
        val slotType = MolangUtils.parseSlotType(context, arguments, 0)
        if (slotType == null || slotType.isArmor) return RESULT_FALSE
        val id = arguments.getAsString(context, 1) ?: return RESULT_FALSE
        val entity = context.entity.entity
        if (id.isBlank()) return RESULT_FALSE
        val itemBySlot = entity.getItemBySlot(slotType)
        if (!handItemPredicate.test(
                entity,
                if (slotType == EquipmentSlot.OFFHAND) InteractionHand.OFF_HAND else InteractionHand.MAIN_HAND
            )
        ) return RESULT_FALSE
        if (itemBySlot.isEmpty && id == EMPTY_ITEM) return RESULT_TRUE
        val strSubstring = id.substring(1)
        when {
            id.startsWith(PREFIX_ITEM_ID) -> {
                val key = BuiltInRegistries.ITEM.getKey(itemBySlot.item)
                return if (strSubstring == key.toString()) RESULT_TRUE else RESULT_FALSE
            }

            id.startsWith(PREFIX_ITEM_TAG) -> {
                val tag = TagKey.create(Registries.ITEM, Identifier.parse(strSubstring))
                return if (itemBySlot.`is`(tag)) RESULT_TRUE else RESULT_FALSE
            }

            id.startsWith(TYPE_PREFIX) -> {
                val itemType = InnerClassify.getItemType(itemBySlot)
                if (itemType.isNotBlank() && (itemType == strSubstring || itemBySlot.useAnimation.name.lowercase(Locale.ENGLISH) == strSubstring))
                    return RESULT_TRUE
                return RESULT_FALSE
            }

            else -> return RESULT_FALSE
        }
    }

    override fun validateArgumentSize(size: Int): Boolean = size == 2 || size == 3

    fun interface HandItemPredicate {
        fun test(livingEntity: LivingEntity, interactionHand: InteractionHand): Boolean
    }

    companion object {
        const val PREFIX_ITEM_ID = "$"
        const val PREFIX_ITEM_TAG = "#"
        const val TYPE_PREFIX = ":"
        const val EMPTY_ITEM = "empty"
        const val RESULT_FALSE = 0
        const val RESULT_TRUE = 1

        fun createAlways(): HandRenderFunction = HandRenderFunction { _, _ -> true }

        fun createWhenSwinging(): HandRenderFunction =
            HandRenderFunction { entity, _ -> entity.swinging && !entity.isSleeping }

        fun createWhenUsing(): HandRenderFunction =
            HandRenderFunction { entity, _ -> entity.isUsingItem && !entity.isSleeping }
    }
}