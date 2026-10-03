package rip.ysm.api.item

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import rip.ysm.api.item.fabric.ToolActionBridgeImpl

object ToolActionBridge {
    @JvmStatic
    fun canFishingRodCast(stack: ItemStack): Boolean = ToolActionBridgeImpl.canFishingRodCast(stack)

    @JvmStatic
    fun onEntitySwing(stack: ItemStack, entity: LivingEntity): Boolean =
        ToolActionBridgeImpl.onEntitySwing(stack, entity)
}
