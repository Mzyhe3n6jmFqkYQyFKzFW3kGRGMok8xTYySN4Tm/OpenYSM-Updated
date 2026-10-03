package com.elfmcys.yesstevemodel.util

import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item

object ThreadLocalItemTagSets {
    @JvmField
    val ITEM_SET: ThreadLocal<ReferenceOpenHashSet<Item>> = ThreadLocal.withInitial { ReferenceOpenHashSet(16) }

    @JvmField
    val TAG_KEY_LIST: ThreadLocal<ReferenceArrayList<TagKey<Item>>> = ThreadLocal.withInitial { ReferenceArrayList(16) }
}