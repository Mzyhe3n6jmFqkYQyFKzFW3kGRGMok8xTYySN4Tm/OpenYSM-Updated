@file:Suppress("unused")

package com.elfmcys.yesstevemodel.client.entity

import com.elfmcys.yesstevemodel.geckolib3.core.EntityFrameStateTracker
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import rip.ysm.compat.immersivemelodies.ImmersiveMelodiesCompat

open class LivingEntityFrameState<T : LivingEntity>(entity: T) : EntityFrameStateTracker<T>(entity) {
    private val imData: ImmersiveMelodiesCompat.ImmersiveMelodiesData = ImmersiveMelodiesCompat.ImmersiveMelodiesData()
    private var mainHandItem: ItemStack = ItemStack.EMPTY
    private var offHandItem: ItemStack = ItemStack.EMPTY

    override fun reset() {
        mainHandItem = ItemStack.EMPTY
        offHandItem = ItemStack.EMPTY
        super.reset()
    }

    override fun onTimeUpdate(currentTick: Float, deltaTick: Float, partialTick: Float) {
        super.onTimeUpdate(currentTick, deltaTick, partialTick)
        ImmersiveMelodiesCompat.updateMelodyProgress(entity, imData)
    }

    open fun getHandItemsForAnimation(interactionHand: InteractionHand): ItemStack {
        if (interactionHand == InteractionHand.MAIN_HAND) return mainHandItem
        return offHandItem
    }

    open fun setHandItemsForAnimation(itemStack: ItemStack, interactionHand: InteractionHand) =
        if (interactionHand == InteractionHand.MAIN_HAND) mainHandItem = itemStack else offHandItem = itemStack

    open val immersiveMelodiesData: ImmersiveMelodiesCompat.ImmersiveMelodiesData
        get() = imData
}