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
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.util.Locale

class HandRenderFunction(private val handItemPredicate: HandItemPredicate) : LivingEntityFunction() {

    override fun eval(context: ExecutionContext<IContext<LivingEntity>>, arguments: ArgumentCollection): Any? {
        val slotType: EquipmentSlot? = MolangUtils.parseSlotType(context, arguments, 0)
        if (slotType == null || slotType.isArmor) {
            return 0
        }
        val id: String = arguments.getAsString(context, 1) ?: return 0
        val entity: LivingEntity = context.entity().entity()
        if (id.isBlank()) {
            return 0
        }
        val itemBySlot: ItemStack = entity.getItemBySlot(slotType)
        if (!handItemPredicate.test(entity, if (slotType == EquipmentSlot.OFFHAND) InteractionHand.OFF_HAND else InteractionHand.MAIN_HAND)) {
            return 0
        }
        if (itemBySlot.isEmpty && id == EMPTY_ITEM) {
            return 1
        }
        val strSubstring: String = id.substring(1)
        if (id.startsWith(PREFIX_ITEM_ID)) {
            val key: Identifier? = BuiltInRegistries.ITEM.getKey(itemBySlot.item) ?: return 0
            return if (strSubstring == key.toString()) 1 else 0
        }
        if (id.startsWith(PREFIX_ITEM_TAG)) {
            val tag: TagKey<Item> = TagKey.create(Registries.ITEM, Identifier.parse(strSubstring))
            return if (itemBySlot.`is`(tag)) 1 else 0
        }
        if (id.startsWith(TYPE_PREFIX)) {
            val itemType: String = InnerClassify.getItemType(itemBySlot)
            if (itemType.isNotBlank() && (itemType == strSubstring || itemBySlot.useAnimation.name.lowercase(Locale.ENGLISH) == strSubstring)) {
                return 1
            }
            return 0
        }
        return 0
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size == 2 || size == 3
    }

    fun interface HandItemPredicate {
        fun test(livingEntity: LivingEntity, interactionHand: InteractionHand): Boolean
    }

    companion object {
        const val PREFIX_ITEM_ID: String = "$"
        const val PREFIX_ITEM_TAG: String = "#"
        const val TYPE_PREFIX: String = ":"
        const val EMPTY_ITEM: String = "empty"
        const val RESULT_FALSE: Int = 0
        const val RESULT_TRUE: Int = 1

        @JvmStatic
        fun createAlways(): HandRenderFunction {
            return HandRenderFunction { _, _ -> true }
        }

        @JvmStatic
        fun createWhenSwinging(): HandRenderFunction {
            return HandRenderFunction { entity, _ -> entity.swinging && !entity.isSleeping }
        }

        @JvmStatic
        fun createWhenUsing(): HandRenderFunction {
            return HandRenderFunction { entity, _ -> entity.isUsingItem && !entity.isSleeping }
        }
    }
}