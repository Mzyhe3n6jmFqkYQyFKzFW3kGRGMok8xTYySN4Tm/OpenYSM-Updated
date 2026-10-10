package rip.ysm.compat.gun.tacz

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import rip.ysm.compat.ModCompat
import rip.ysm.compat.gun.tacz.fabric.TacCompatImpl

object TacCompat : ModCompat("tacz") {
    fun registerControllerFunctions(binding: CtrlBinding) {
        if (!isModLoaded) return
        TacCompatImpl.registerControllerFunctions(binding)
    }

    fun applyItemTransform(
        stack: ItemStack,
        model: AnimatedGeoModel,
        entity: LivingEntity,
        poseStack: PoseStack,
        packedLightIn: Int,
        partialTicks: Float
    ) {
        if (!isModLoaded) return
        TacCompatImpl.applyItemTransform(stack, model, entity, poseStack, packedLightIn, partialTicks)
    }

    fun handleTaczAnimState(
        entity: LivingEntity,
        event: AnimationEvent<out LivingAnimatable<*>>,
        animation: String,
        loopType: ILoopType
    ): PlayState? {
        if (!isModLoaded) return null
        return TacCompatImpl.handleTaczAnimState(entity, event, animation, loopType)
    }

    fun handleGunHoldAnimState(
        stack: ItemStack,
        event: AnimationEvent<out LivingAnimatable<*>>
    ): PlayState? {
        if (!isModLoaded) return null
        return TacCompatImpl.handleGunHoldAnimState(stack, event)
    }

    fun handleGunActionAnimState(
        stack: ItemStack,
        event: AnimationEvent<out LivingAnimatable<*>>
    ): PlayState? {
        if (!isModLoaded) return null
        return TacCompatImpl.handleGunActionAnimState(stack, event)
    }

    fun handleGunSound(entity: LivingEntity, stack: ItemStack) {
        if (!isModLoaded) return
        TacCompatImpl.handleGunSound(entity, stack)
    }

    fun handleItemSound(stack: ItemStack) {
        if (!isModLoaded) return
        TacCompatImpl.handleItemSound(stack)
    }

    fun getGunTexture(stack: ItemStack): Identifier? {
        if (!isModLoaded) return null
        return TacCompatImpl.getGunTexture(stack)
    }
}
