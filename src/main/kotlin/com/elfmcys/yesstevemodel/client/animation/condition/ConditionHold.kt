package com.elfmcys.yesstevemodel.client.animation.condition

import com.elfmcys.yesstevemodel.util.EquipmentUtil
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.tags.TagKey
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemUseAnimation
import java.util.*

class ConditionHold(hand: InteractionHand) {
    private val preSize: Int = if (hand == InteractionHand.MAIN_HAND) 14 else 13
    private val idPre: String = if (hand == InteractionHand.MAIN_HAND) "hold_mainhand$" else "hold_offhand$"
    private val tagPre: String = if (hand == InteractionHand.MAIN_HAND) "hold_mainhand#" else "hold_offhand#"
    private val extraPre: String = if (hand == InteractionHand.MAIN_HAND) "hold_mainhand:" else "hold_offhand:"
    private val idTest: ObjectOpenHashSet<Identifier> = ObjectOpenHashSet()
    private val tagTest: ReferenceArrayList<TagKey<Item>> = ReferenceArrayList()
    private val extraTes: ReferenceOpenHashSet<ItemUseAnimation> = ReferenceOpenHashSet()
    private val innerTest: ObjectOpenHashSet<String> = ObjectOpenHashSet()

    fun addTest(name: String) {
        if (name.length <= preSize) return
        val strSubstring = name.substring(preSize)
        if (name.startsWith(idPre) && Identifier.tryParse(strSubstring) != null)
            idTest.add(Identifier.parse(strSubstring))
        if (name.startsWith(tagPre) && Identifier.tryParse(strSubstring) != null)
            tagTest.add(TagKey.create(Registries.ITEM, Identifier.parse(strSubstring)))
        if (!name.startsWith(extraPre) || strSubstring == ItemUseAnimation.NONE.name.lowercase(Locale.US)) return
        EquipmentUtil.getItemUseAnimation(strSubstring)?.let { extraTes.add(it) }
        innerTest.add(name)
    }

    fun doTest(entity: LivingEntity, hand: InteractionHand): String {
        if (entity.getItemInHand(hand).isEmpty)
            return if (hand == InteractionHand.MAIN_HAND) EMPTY_MAINHAND else EMPTY_OFFHAND
        var result = doIdTest(entity, hand)
        if (result.isEmpty()) {
            result = doTagTest(entity, hand)
            if (result.isEmpty()) {
                return doExtraTest(entity, hand)
            }
            return result
        }
        return result
    }

    private fun doIdTest(livingEntity: LivingEntity, interactionHand: InteractionHand): String {
        if (idTest.isEmpty()) {
            return EMPTY
        }
        val key = BuiltInRegistries.ITEM.getKey(livingEntity.getItemInHand(interactionHand).item)
        if (idTest.contains(key)) return idPre + key
        return EMPTY
    }

    private fun doTagTest(livingEntity: LivingEntity, interactionHand: InteractionHand): String {
        if (tagTest.isEmpty) return EMPTY
        val itemInHand = livingEntity.getItemInHand(interactionHand)
        return tagTest.firstOrNull { itemInHand.`is`(it) }?.let { tagPre + it.location() } ?: EMPTY
    }

    private fun doExtraTest(entity: LivingEntity, hand: InteractionHand): String {
        if (extraTes.isEmpty() && innerTest.isEmpty()) return EMPTY
        val innerName = InnerClassify.doClassifyTest(extraPre, entity, hand)
        if (innerName.isNotBlank() && innerTest.contains(innerName)) return innerName
        val anim = entity.getItemInHand(hand).useAnimation
        if (extraTes.contains(anim)) return extraPre + anim.name.lowercase(Locale.US)
        return EMPTY
    }

    companion object {
        const val EMPTY_MAINHAND: String = "hold_mainhand:empty"
        const val EMPTY_OFFHAND: String = "hold_offhand:empty"
        const val EMPTY: String = ""
    }
}