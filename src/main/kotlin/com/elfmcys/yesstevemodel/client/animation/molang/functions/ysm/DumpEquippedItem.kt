package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.LivingEntityFunction
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function.ArgumentCollection
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentUtils
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.ItemEnchantments
import rip.ysm.compat.cosmeticarmorreworked.CosmeticArmorHelper
import kotlin.jvm.optionals.getOrNull

class DumpEquippedItem : LivingEntityFunction() {
    override fun eval(context: ExecutionContext<IContext<LivingEntity>>, arguments: ArgumentCollection): Any? {
        if (!context.entity.isDebugMode) return null
        val slot = MolangUtils.parseSlotType(context, arguments, 0) ?: return null
        val stack = CosmeticArmorHelper.getArmorItem(context.entity.entity, slot)
        if (stack.isEmpty) return null
        val key = BuiltInRegistries.ITEM.getKey(stack.item)
        context.entity.logWarningComponent(
            Component.literal("Display ")
                .append(ComponentUtils.copyOnClickText(stack.item.getName(stack).getString(99)))
        )
        context.entity
            .logWarningComponent(Component.literal("Name ").append(ComponentUtils.copyOnClickText(key.toString())))
        stack.tags.forEach { tagKey ->
            context.entity.logWarningComponent(
                Component.literal("Tag ").append(ComponentUtils.copyOnClickText(tagKey.location().toString()))
            )
        }
        val enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)
        for (entry in enchantments.entrySet()) {
            val holder = entry.key
            val lvl = entry.intValue
            val name = holder.unwrapKey().map { it.identifier() }.getOrNull()
            if (name != null) {
                context.entity.logWarningComponent(
                    Component.literal("Enchantment: display ")
                        .append(ComponentUtils.copyOnClickText(Enchantment.getFullname(holder, lvl).getString(99)))
                        .append(Component.literal("  name "))
                        .append(ComponentUtils.copyOnClickText(name.toString()))
                )
            }
        }
        return null
    }

    override fun validateArgumentSize(size: Int): Boolean = size == 1
}