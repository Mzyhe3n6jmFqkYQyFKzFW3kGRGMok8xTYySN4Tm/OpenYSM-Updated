package com.elfmcys.yesstevemodel.util

import com.elfmcys.yesstevemodel.NameSpaces
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item

object ItemTagsConstants {
    val AXES: TagKey<Item> = createTag("axes")

    val HOES: TagKey<Item> = createTag("hoes")

    val PICKAXES: TagKey<Item> = createTag("pickaxes")

    val SHOVELS: TagKey<Item> = createTag("shovels")

    val SWORDS: TagKey<Item> = createTag("swords")

    val THROWABLE_POTION: TagKey<Item> = createTag("throwable_potion")

    val BOWS: TagKey<Item> = createTag("bows")

    val CROSSBOWS: TagKey<Item> = createTag("crossbows")

    val FISHING_RODS: TagKey<Item> = createTag("fishing_rods")

    val SHIELDS: TagKey<Item> = createTag("shields")

    val TRIDENTS: TagKey<Item> = createTag("tridents")

    val SLASHBLADE: TagKey<Item> = createTag("slashblade")

    val MACE: TagKey<Item> = createTag("mace")

    val PIKE: TagKey<Item> = createTag("pike")

    private fun createTag(str: String): TagKey<Item> {
        return TagKey.create(Registries.ITEM, NameSpaces.MOD.path(str))
    }
}