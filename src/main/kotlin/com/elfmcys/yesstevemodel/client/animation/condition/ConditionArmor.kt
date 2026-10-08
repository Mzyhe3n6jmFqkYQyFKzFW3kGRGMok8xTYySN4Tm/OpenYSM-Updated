package com.elfmcys.yesstevemodel.client.animation.condition

import com.elfmcys.yesstevemodel.util.EquipmentUtil
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap
import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import rip.ysm.compat.cosmeticarmorreworked.CosmeticArmorHelper
import java.util.regex.Pattern

class ConditionArmor {
    private val idTest: Reference2ReferenceOpenHashMap<EquipmentSlot, ObjectOpenHashSet<Identifier>> =
        Reference2ReferenceOpenHashMap()
    private val tagTest: Reference2ReferenceOpenHashMap<EquipmentSlot, ReferenceArrayList<TagKey<Item>>> =
        Reference2ReferenceOpenHashMap()

    fun addTest(str: String) {
        run {
            val matcher = ID_PRE_REG.matcher(str)
            if (matcher.find()) {
                val slot = getType(matcher.group(1))
                if (slot != null) {
                    val strGroup = matcher.group(2)
                    if (Identifier.tryParse(strGroup) != null) {
                        idTest.computeIfAbsent(slot) { ObjectOpenHashSet() }.add(Identifier.parse(strGroup))
                    }
                }
            }
        }

        run {
            val matcher = TAG_PRE_REG.matcher(str)
            if (matcher.find()) {
                val slot = getType(matcher.group(1))
                if (slot != null) {
                    val strGroup2 = matcher.group(2)
                    if (Identifier.tryParse(strGroup2) != null) {
                        tagTest.computeIfAbsent(slot) { ReferenceArrayList() }
                            .add(TagKey.create(Registries.ITEM, Identifier.parse(strGroup2)))
                    }
                }
            }
        }
    }

    fun doTest(entity: LivingEntity, slot: EquipmentSlot): String {
        if (CosmeticArmorHelper.getArmorItem(entity, slot).isEmpty) return EMPTY
        val result = doIdTest(entity, slot)
        if (result.isEmpty()) return doTagTest(entity, slot)
        return result
    }

    private fun doIdTest(livingEntity: LivingEntity, equipmentSlot: EquipmentSlot): String {
        val set = idTest[equipmentSlot]
        if (idTest.isEmpty() || set.isNullOrEmpty()) return EMPTY
        val key = BuiltInRegistries.ITEM.getKey(CosmeticArmorHelper.getArmorItem(livingEntity, equipmentSlot).item)
        if (set.contains(key)) return "${equipmentSlot.getName()}$$key"
        return EMPTY
    }

    private fun doTagTest(livingEntity: LivingEntity, equipmentSlot: EquipmentSlot): String {
        val list = tagTest[equipmentSlot]
        if (tagTest.isEmpty() || list.isNullOrEmpty()) return EMPTY
        val stack = CosmeticArmorHelper.getArmorItem(livingEntity, equipmentSlot)
        return list.firstOrNull { stack.`is`(it) }?.let { "${equipmentSlot.getName()}#${it.location()}" } ?: EMPTY
    }

    fun hasFilter(equipmentSlot: EquipmentSlot): Boolean =
        tagTest.containsKey(equipmentSlot) || idTest.containsKey(equipmentSlot)

    companion object {
        private val ID_PRE_REG: Pattern = Pattern.compile("^(.+?)\\$(.*?)$")
        private val TAG_PRE_REG: Pattern = Pattern.compile("^(.+?)#(.*?)$")
        const val EMPTY: String = ""

        @JvmStatic
        fun getType(type: String): EquipmentSlot? = EquipmentUtil.getEquipmentSlot(type)
    }
}