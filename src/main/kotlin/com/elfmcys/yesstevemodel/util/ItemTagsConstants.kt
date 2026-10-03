package com.elfmcys.yesstevemodel.util

import com.elfmcys.yesstevemodel.NameSpaces
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item

object ItemTagsConstants {
    @JvmField
    val AXES: TagKey<Item> = createTag("axes")

    @JvmField
    val HOES: TagKey<Item> = createTag("hoes")

    @JvmField
    val PICKAXES: TagKey<Item> = createTag("pickaxes")

    @JvmField
    val SHOVELS: TagKey<Item> = createTag("shovels")

    @JvmField
    val SWORDS: TagKey<Item> = createTag("swords")

    @JvmField
    val THROWABLE_POTION: TagKey<Item> = createTag("throwable_potion")

    @JvmField
    val BOWS: TagKey<Item> = createTag("bows")

    @JvmField
    val CROSSBOWS: TagKey<Item> = createTag("crossbows")

    @JvmField
    val FISHING_RODS: TagKey<Item> = createTag("fishing_rods")

    @JvmField
    val SHIELDS: TagKey<Item> = createTag("shields")

    @JvmField
    val TRIDENTS: TagKey<Item> = createTag("tridents")

    @JvmField
    val SLASHBLADE: TagKey<Item> = createTag("slashblade")

    @JvmField
    val MACE: TagKey<Item> = createTag("mace")

    @JvmField
    val PIKE: TagKey<Item> = createTag("pike")

    private fun createTag(str: String): TagKey<Item> {
        return TagKey.create(Registries.ITEM, NameSpaces.MOD.path(str))
    }
}