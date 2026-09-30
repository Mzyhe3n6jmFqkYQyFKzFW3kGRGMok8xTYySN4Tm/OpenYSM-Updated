package rip.ysm.compat.gun.swarfare

import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import rip.ysm.compat.gun.swarfare.fabric.SWarfareCompatImpl

object SWarfareCompat {
    @JvmStatic
    fun isLoaded(): Boolean = SWarfareCompatImpl.isLoaded()

    @JvmStatic
    fun isGunItem(itemStack: ItemStack): Boolean = SWarfareCompatImpl.isGunItem(itemStack)

    @JvmStatic
    fun isPlayerAiming(player: Player): Boolean = SWarfareCompatImpl.isPlayerAiming(player)

    @JvmStatic
    fun applyGunTransform(
        stack: ItemStack,
        model: AnimatedGeoModel,
        entity: LivingEntity,
        poseStack: PoseStack,
        packedLightIn: Int,
        partialTicks: Float
    ) {
        SWarfareCompatImpl.applyGunTransform(stack, model, entity, poseStack, packedLightIn, partialTicks)
    }

    @JvmStatic
    fun handleTaczAnim(
        entity: LivingEntity,
        event: AnimationEvent<out LivingAnimatable<out LivingEntity>>,
        str: String,
        loopType: ILoopType
    ): PlayState = SWarfareCompatImpl.handleTaczAnim(entity, event, str, loopType)

    @JvmStatic
    fun handleGunHoldAnim(
        stack: ItemStack,
        event: AnimationEvent<out LivingAnimatable<out LivingEntity>>
    ): PlayState = SWarfareCompatImpl.handleGunHoldAnim(stack, event)

    @JvmStatic
    fun handleGunActionAnim(
        stack: ItemStack,
        event: AnimationEvent<out LivingAnimatable<out LivingEntity>>
    ): PlayState = SWarfareCompatImpl.handleGunActionAnim(stack, event)

    @JvmStatic
    fun getGunTexture(stack: ItemStack): Identifier? = SWarfareCompatImpl.getGunTexture(stack)
}
