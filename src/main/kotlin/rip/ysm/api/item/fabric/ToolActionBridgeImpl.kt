package rip.ysm.api.item.fabric

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.FishingRodItem
import net.minecraft.world.item.ItemStack

object ToolActionBridgeImpl {
    fun canFishingRodCast(stack: ItemStack): Boolean = stack.item is FishingRodItem

    fun onEntitySwing(stack: ItemStack, entity: LivingEntity): Boolean = false
}
