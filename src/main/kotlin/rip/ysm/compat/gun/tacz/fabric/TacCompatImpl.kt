package rip.ysm.compat.gun.tacz.fabric

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

object TacCompatImpl : ModCompat("tacz") {
    @JvmStatic
    fun registerControllerFunctions(binding: CtrlBinding) {
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
    }

    @JvmStatic
    fun handleTaczAnimState(
        entity: LivingEntity,
        event: AnimationEvent<out LivingAnimatable<*>>,
        animation: String,
        loopType: ILoopType
    ): PlayState? = null

    @JvmStatic
    fun handleGunHoldAnimState(
        stack: ItemStack,
        event: AnimationEvent<out LivingAnimatable<*>>
    ): PlayState? = null

    @JvmStatic
    fun handleGunActionAnimState(
        stack: ItemStack,
        event: AnimationEvent<out LivingAnimatable<*>>
    ): PlayState? = null

    @JvmStatic
    fun handleGunSound(entity: LivingEntity, stack: ItemStack) {
    }

    @JvmStatic
    fun handleItemSound(stack: ItemStack) {
    }

    @JvmStatic
    fun getGunTexture(stack: ItemStack): Identifier? = null
}
