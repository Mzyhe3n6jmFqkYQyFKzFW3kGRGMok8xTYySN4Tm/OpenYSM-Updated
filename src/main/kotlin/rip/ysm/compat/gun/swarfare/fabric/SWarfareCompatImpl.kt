package rip.ysm.compat.gun.swarfare.fabric

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

object SWarfareCompatImpl : ModCompat("superbwarfare") {
    fun isGunItem(itemStack: ItemStack): Boolean = false

    fun isPlayerAiming(player: Player): Boolean = false

    fun applyGunTransform(
        stack: ItemStack,
        model: AnimatedGeoModel,
        entity: LivingEntity,
        poseStack: PoseStack,
        packedLightIn: Int,
        partialTicks: Float
    ) {
    }

    fun handleTaczAnim(
        entity: LivingEntity,
        event: AnimationEvent<*>,
        str: String,
        loopType: ILoopType
    ): PlayState? = null

    fun handleGunHoldAnim(
        stack: ItemStack,
        event: AnimationEvent<*>
    ): PlayState? = null

    fun handleGunActionAnim(
        stack: ItemStack,
        event: AnimationEvent<*>
    ): PlayState? = null

    fun getGunTexture(stack: ItemStack): Identifier? = null
}
