package com.elfmcys.yesstevemodel.client.animation.condition

import com.elfmcys.yesstevemodel.util.ItemTagsConstants
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.*
import rip.ysm.compat.slashblade.SlashBladeCompat
import rip.ysm.compat.touhoulittlemaid.TouhouLittleMaidCompat

object InnerClassify {
    const val EMPTY: String = ""

    fun doClassifyTest(str: String, livingEntity: LivingEntity, interactionHand: InteractionHand): String {
        val itemType: String = getItemType(livingEntity.getItemInHand(interactionHand))
        if (itemType.isNotEmpty()) return str + itemType
        return ""
    }

    fun getItemType(itemStack: ItemStack): String {
        val item: Item = itemStack.item
        when {
            SlashBladeCompat.isSlashBladeItem(itemStack) -> return "slashblade"
            itemStack.`is`(ItemTagsConstants.SWORDS) -> return "sword"
            item is MaceItem || itemStack.`is`(ItemTagsConstants.MACE) -> return "mace"
            TouhouLittleMaidCompat.isMaidItem(item) -> return "gohei"
            item is AxeItem || itemStack.`is`(ItemTagsConstants.AXES) -> return "axe"
            itemStack.`is`(ItemTagsConstants.PICKAXES) -> return "pickaxe"
            item is ShovelItem || itemStack.`is`(ItemTagsConstants.SHOVELS) -> return "shovel"
            item is HoeItem || itemStack.`is`(ItemTagsConstants.HOES) -> return "hoe"
            item is ShieldItem || itemStack.`is`(ItemTagsConstants.SHIELDS) -> return "shield"
            item is CrossbowItem || itemStack.`is`(ItemTagsConstants.CROSSBOWS) -> return "crossbow"
            item is BowItem || itemStack.`is`(ItemTagsConstants.BOWS) -> return "bow"
            item is FishingRodItem || itemStack.`is`(ItemTagsConstants.FISHING_RODS) -> return "fishing_rod"
            item is TridentItem || itemStack.`is`(ItemTagsConstants.TRIDENTS) -> return "spear"
            itemStack.`is`(ItemTagsConstants.PIKE) -> return "lance"
            item is ThrowablePotionItem || itemStack.`is`(ItemTagsConstants.THROWABLE_POTION) -> return "throwable_potion"
            else -> return ""
        }
    }
}