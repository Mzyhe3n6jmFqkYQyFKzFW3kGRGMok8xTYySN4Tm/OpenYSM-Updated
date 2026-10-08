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
        if (slotType == null || slotType.isArmor) return 0
        val id = arguments.getAsString(context, 1) ?: return 0
        val entity = context.entity.entity
        if (id.isBlank()) return 0
        val itemBySlot = entity.getItemBySlot(slotType)
        if (!handItemPredicate.test(
                entity,
                if (slotType == EquipmentSlot.OFFHAND) InteractionHand.OFF_HAND else InteractionHand.MAIN_HAND
            )
        ) return 0
        if (itemBySlot.isEmpty && id == EMPTY_ITEM) return 1
        val strSubstring = id.substring(1)
        when {
            id.startsWith(PREFIX_ITEM_ID) -> {
                val key = BuiltInRegistries.ITEM.getKey(itemBySlot.item)
                return if (strSubstring == key.toString()) 1 else 0
            }

            id.startsWith(PREFIX_ITEM_TAG) -> {
                val tag = TagKey.create(Registries.ITEM, Identifier.parse(strSubstring))
                return if (itemBySlot.`is`(tag)) 1 else 0
            }

            id.startsWith(TYPE_PREFIX) -> {
                val itemType = InnerClassify.getItemType(itemBySlot)
                if (itemType.isNotBlank() && (itemType == strSubstring || itemBySlot.useAnimation.name.lowercase(Locale.ENGLISH) == strSubstring))
                    return 1
                return 0
            }

            else -> return 0
        }
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size == 2 || size == 3
    }

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

        @JvmStatic
        fun createAlways(): HandRenderFunction = HandRenderFunction { _, _ -> true }

        @JvmStatic
        fun createWhenSwinging(): HandRenderFunction =
            HandRenderFunction { entity, _ -> entity.swinging && !entity.isSleeping }

        @JvmStatic
        fun createWhenUsing(): HandRenderFunction =
            HandRenderFunction { entity, _ -> entity.isUsingItem && !entity.isSleeping }
    }
}