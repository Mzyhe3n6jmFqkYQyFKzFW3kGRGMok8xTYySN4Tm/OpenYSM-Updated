@file:Suppress("unused")

package com.elfmcys.yesstevemodel.util

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemUseAnimation
import org.apache.commons.lang3.EnumUtils
import java.util.*

object EquipmentUtil {
    private val SLOT_BY_NAME: Object2ReferenceOpenHashMap<String, EquipmentSlot> =
        Object2ReferenceOpenHashMap<String, EquipmentSlot>().apply {
            for (slot in EquipmentSlot.entries) {
                put(slot.getName().lowercase(Locale.US), slot)
            }
        }

    fun getItemUseAnimationByName(str: String): Optional<ItemUseAnimation> =
        Optional.ofNullable(getItemUseAnimation(str))

    fun getItemUseAnimation(str: String): ItemUseAnimation? =
        EnumUtils.getEnum(ItemUseAnimation::class.java, str.uppercase(Locale.US))

    fun getEquipmentSlotByName(str: String): Optional<EquipmentSlot> = Optional.ofNullable(getEquipmentSlot(str))

    fun getEquipmentSlot(str: String): EquipmentSlot? = SLOT_BY_NAME[str]
}