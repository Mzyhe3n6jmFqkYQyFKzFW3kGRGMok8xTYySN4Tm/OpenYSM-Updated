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
import rip.ysm.compat.gun.tacz.fabric.TacCompatImpl

object TacCompat {
    @JvmStatic
    fun isLoaded(): Boolean = TacCompatImpl.isLoaded()

    @JvmStatic
    fun registerControllerFunctions(binding: CtrlBinding) {
        TacCompatImpl.registerControllerFunctions(binding)
    }

    @JvmStatic
    fun applyItemTransform(
        stack: ItemStack,
        model: AnimatedGeoModel,
        entity: LivingEntity,
        poseStack: PoseStack,
        packedLightIn: Int,
        partialTicks: Float
    ) {
        TacCompatImpl.applyItemTransform(stack, model, entity, poseStack, packedLightIn, partialTicks)
    }

    @JvmStatic
    fun handleTaczAnimState(
        entity: LivingEntity,
        event: AnimationEvent<out LivingAnimatable<*>>,
        animation: String,
        loopType: ILoopType
    ): PlayState? = TacCompatImpl.handleTaczAnimState(entity, event, animation, loopType)

    @JvmStatic
    fun handleGunHoldAnimState(
        stack: ItemStack,
        event: AnimationEvent<out LivingAnimatable<*>>
    ): PlayState? = TacCompatImpl.handleGunHoldAnimState(stack, event)

    @JvmStatic
    fun handleGunActionAnimState(
        stack: ItemStack,
        event: AnimationEvent<out LivingAnimatable<*>>
    ): PlayState? = TacCompatImpl.handleGunActionAnimState(stack, event)

    @JvmStatic
    fun handleGunSound(entity: LivingEntity, stack: ItemStack) {
        TacCompatImpl.handleGunSound(entity, stack)
    }

    @JvmStatic
    fun handleItemSound(stack: ItemStack) {
        TacCompatImpl.handleItemSound(stack)
    }

    @JvmStatic
    fun getGunTexture(stack: ItemStack): Identifier? = TacCompatImpl.getGunTexture(stack)
}
