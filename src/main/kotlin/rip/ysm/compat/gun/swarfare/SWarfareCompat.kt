package rip.ysm.compat.gun.swarfare

import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import rip.ysm.compat.ModCompat
import rip.ysm.compat.gun.swarfare.fabric.SWarfareCompatImpl

object SWarfareCompat : ModCompat("superbwarfare") {
    @JvmStatic
    fun isGunItem(itemStack: ItemStack): Boolean {
        return isModLoaded && SWarfareCompatImpl.isGunItem(itemStack)
    }

    @JvmStatic
    fun isPlayerAiming(player: Player): Boolean {
        return isModLoaded && SWarfareCompatImpl.isPlayerAiming(player)
    }

    @JvmStatic
    fun applyGunTransform(
        stack: ItemStack,
        model: AnimatedGeoModel,
        entity: LivingEntity,
        poseStack: PoseStack,
        packedLightIn: Int,
        partialTicks: Float
    ) {
        if (!isModLoaded) return
        SWarfareCompatImpl.applyGunTransform(stack, model, entity, poseStack, packedLightIn, partialTicks)
    }

    @JvmStatic
    fun handleTaczAnim(
        entity: LivingEntity,
        event: AnimationEvent<out LivingAnimatable<out LivingEntity>>,
        str: String,
        loopType: ILoopType
    ): PlayState? {
        if (!isModLoaded) return null
        return SWarfareCompatImpl.handleTaczAnim(entity, event, str, loopType)
    }

    @JvmStatic
    fun handleGunHoldAnim(
        stack: ItemStack,
        event: AnimationEvent<out LivingAnimatable<out LivingEntity>>
    ): PlayState? {
        if (!isModLoaded) return null
        return SWarfareCompatImpl.handleGunHoldAnim(stack, event)
    }

    @JvmStatic
    fun handleGunActionAnim(
        stack: ItemStack,
        event: AnimationEvent<out LivingAnimatable<out LivingEntity>>
    ): PlayState? {
        if (!isModLoaded) return null
        return SWarfareCompatImpl.handleGunActionAnim(stack, event)
    }

    @JvmStatic
    fun getGunTexture(stack: ItemStack): Identifier? {
        if (!isModLoaded) return null
        return SWarfareCompatImpl.getGunTexture(stack)
    }
}
