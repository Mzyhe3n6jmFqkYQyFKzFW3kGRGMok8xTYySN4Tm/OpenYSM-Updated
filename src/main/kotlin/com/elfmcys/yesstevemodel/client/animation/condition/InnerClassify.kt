package com.elfmcys.yesstevemodel.client.animation.condition

import com.elfmcys.yesstevemodel.util.ItemTagsConstants
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.*
import rip.ysm.compat.slashblade.SlashBladeCompat
import rip.ysm.compat.touhoulittlemaid.TouhouLittleMaidCompat

object InnerClassify {
    const val EMPTY: String = ""

    @JvmStatic
    fun doClassifyTest(str: String, livingEntity: LivingEntity, interactionHand: InteractionHand): String {
        val itemType: String = getItemType(livingEntity.getItemInHand(interactionHand))
        if (itemType.isNotEmpty()) {
            return str + itemType
        }
        return ""
    }

    @JvmStatic
    fun getItemType(itemStack: ItemStack): String {
        val item: Item = itemStack.item
        if (SlashBladeCompat.isSlashBladeItem(itemStack)) {
            return "slashblade"
        }
        if (itemStack.`is`(ItemTagsConstants.SWORDS)) {
            return "sword"
        }
        if (item is MaceItem || itemStack.`is`(ItemTagsConstants.MACE)) {
            return "mace"
        }
        if (TouhouLittleMaidCompat.isMaidItem(item)) {
            return "gohei"
        }
        if (item is AxeItem || itemStack.`is`(ItemTagsConstants.AXES)) {
            return "axe"
        }
        if (itemStack.`is`(ItemTagsConstants.PICKAXES)) {
            return "pickaxe"
        }
        if (item is ShovelItem || itemStack.`is`(ItemTagsConstants.SHOVELS)) {
            return "shovel"
        }
        if (item is HoeItem || itemStack.`is`(ItemTagsConstants.HOES)) {
            return "hoe"
        }
        if (item is ShieldItem || itemStack.`is`(ItemTagsConstants.SHIELDS)) {
            return "shield"
        }
        if (item is CrossbowItem || itemStack.`is`(ItemTagsConstants.CROSSBOWS)) {
            return "crossbow"
        }
        if (item is BowItem || itemStack.`is`(ItemTagsConstants.BOWS)) {
            return "bow"
        }
        if (item is FishingRodItem || itemStack.`is`(ItemTagsConstants.FISHING_RODS)) {
            return "fishing_rod"
        }
        if (item is TridentItem || itemStack.`is`(ItemTagsConstants.TRIDENTS)) {
            return "spear"
        }
        if (itemStack.`is`(ItemTagsConstants.PIKE)) {
            return "lance"
        }
        if (item is ThrowablePotionItem || itemStack.`is`(ItemTagsConstants.THROWABLE_POTION)) {
            return "throwable_potion"
        }
        return ""
    }
}