package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin

import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.client.entity.PlayerEntityFrameState
import com.elfmcys.yesstevemodel.geckolib3.core.EntityFrameStateTracker
import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ContextBinding
import com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.query.*
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils
import com.elfmcys.yesstevemodel.util.CameraUtil
import net.minecraft.client.CameraType
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.client.player.LocalPlayer
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.PlayerModelPart
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemUseAnimation
import net.minecraft.world.phys.Vec3
import rip.ysm.compat.cosmeticarmorreworked.CosmeticArmorHelper

object QueryBinding : ContextBinding() {
    init {
        function("debug_output", DebugOut())
        function("biome_has_all_tags", BiomeHasAllTags())
        function("biome_has_any_tag", BiomeHasAnyTag())
        function("relative_block_has_all_tags", RelativeBlockHasAllTags())
        function("relative_block_has_any_tag", RelativeBlockHasAnyTag())
        function("is_item_name_any", IsItemNameAny())
        function("equipped_item_all_tags", EquippedItemAllTags())
        function("equipped_item_any_tag", EquipmentItemAnyTag())
        function("position", Position())
        function("position_delta", PositionDelta())
        function("rotation_to_camera", RotationToCamera())
        function("max_durability", MaxDurability())
        function("remaining_durability", RemainingDurability())

        `var`("actor_count") { ctx -> ctx.level?.entityCount ?: 0 }
        `var`("anim_time") { ctx -> ctx.animationControllerContext?.animTime ?: 0.0f }
        `var`("all_animations_finished") { ctx -> ctx.playbackFlags?.isPaused ?: false }
        `var`("any_animation_finished") { ctx -> ctx.playbackFlags?.isStopped ?: false }
        `var`("life_time") { ctx -> ctx.geoInstance.seekTime / 20.0 }
        `var`("head_x_rotation") { ctx -> ctx.data.netHeadYaw }
        `var`("head_y_rotation") { ctx -> ctx.data.headPitch }
        `var`("moon_phase") { ctx -> ((ctx.level?.dayTime ?: 0L) / 24000L % 8L).toInt() }
        `var`("time_of_day") { ctx -> MolangUtils.normalizeTime(ctx.level?.dayTime ?: 0L) }
        `var`("time_stamp") { ctx -> ctx.level?.dayTime ?: 0L }
        `var`("delta_time") { ctx -> ctx.geoInstance.positionTracker.getTimeDelta() / 20.0f }

        entityVar("yaw_speed", QueryBinding::getYawSpeed)
        entityVar("cardinal_facing_2d") { ctx -> ctx.entity.direction.get3DDataValue() }
        entityVar("distance_from_camera") { ctx ->
            ctx.mc.gameRenderer.mainCamera.position().distanceTo(ctx.entity.position())
        }
        entityVar("eye_target_x_rotation") { ctx -> ctx.entity.getViewXRot(ctx.animationEvent.partialTick) }
        entityVar("eye_target_y_rotation") { ctx -> ctx.entity.getViewYRot(ctx.animationEvent.partialTick) }
        entityVar("ground_speed") { ctx -> getGroundSpeed(ctx.entity) }
        entityVar("modified_distance_moved") { ctx -> ctx.entity.moveDist }
        entityVar("vertical_speed", QueryBinding::getVerticalSpeed)
        entityVar("walk_distance") { ctx -> ctx.entity.moveDist }
        entityVar("has_rider") { ctx -> ctx.entity.isVehicle }
        entityVar("is_first_person") { ctx -> CameraUtil.getCameraType(ctx) == CameraType.FIRST_PERSON.ordinal }
        entityVar("is_in_water") { ctx -> ctx.entity.isInWater }
        entityVar("is_in_water_or_rain") { ctx -> ctx.entity.isInWaterOrRain }
        entityVar("is_on_fire") { ctx -> ctx.entity.isOnFire }
        entityVar("is_on_ground") { ctx -> ctx.entity.onGround() }
        entityVar("is_riding") { ctx -> ctx.entity.isPassenger }
        entityVar("is_sneaking") { ctx -> ctx.entity.onGround() && ctx.entity.pose == Pose.CROUCHING }
        entityVar("is_spectator") { ctx -> ctx.entity.isSpectator }
        entityVar("is_sprinting") { ctx -> ctx.entity.isSprinting }
        entityVar("is_swimming") { ctx -> ctx.entity.isSwimming }

        livingEntityVar("body_x_rotation") { ctx ->
            Mth.lerp(
                ctx.animationEvent.frameTime,
                ctx.entity.xRotO,
                ctx.entity.xRot
            )
        }
        livingEntityVar("body_y_rotation") { ctx ->
            Mth.wrapDegrees(
                Mth.lerp(
                    ctx.animationEvent.partialTick,
                    ctx.entity.yBodyRotO,
                    ctx.entity.yBodyRot
                )
            )
        }
        livingEntityVar("health", QueryBinding::getHealth)
        livingEntityVar("max_health", QueryBinding::getMaxHealth)
        livingEntityVar("hurt_time") { ctx -> ctx.entity.hurtTime }
        livingEntityVar("is_eating") { ctx -> ctx.entity.useItem.useAnimation == ItemUseAnimation.EAT }
        livingEntityVar("is_playing_dead") { ctx -> ctx.entity.isDeadOrDying }
        livingEntityVar("is_sleeping") { ctx -> ctx.entity.isSleeping }
        livingEntityVar("is_using_item") { ctx -> ctx.entity.isUsingItem }
        livingEntityVar("item_in_use_duration") { ctx -> ctx.entity.ticksUsingItem / 20.0 }
        livingEntityVar("item_max_use_duration") { ctx -> getItemMaxUseDuration(ctx.entity) / 20.0 }
        livingEntityVar("item_remaining_use_duration") { ctx -> ctx.entity.useItemRemainingTicks / 20.0 }
        livingEntityVar("equipment_count") { ctx -> getEquipmentCount(ctx.entity) }

        playerEntityVar("cape_flap_amount", QueryBinding::getCapeFlapAmount)
        playerEntityVar("player_level", QueryBinding::getPlayerLevel)
        playerEntityVar("is_jumping") { ctx ->
            !isFlying(ctx) && !ctx.entity.isPassenger && !ctx.entity.onGround() && !ctx.entity.isInWater
        }

        clientPlayerEntityVar("has_cape") { ctx -> hasCape(ctx.entity) }
    }

    @JvmStatic
    fun isFlying(context: IContext<Player>): Boolean {
        val geoInstance = context.geoInstance
        if (geoInstance is PlayerCapability) {
            if (!geoInstance.isLocalPlayerModel) return geoInstance.positionTracker.isFlying
        }
        return context.entity.abilities.flying
    }

    @JvmStatic
    fun getPlayerLevel(context: IContext<Player>): Int {
        val geoInstance = context.geoInstance
        if (geoInstance is PlayerCapability) {
            if (!geoInstance.isLocalPlayerModel) return geoInstance.positionTracker.experienceLevel
        }
        return context.entity.experienceLevel
    }

    @JvmStatic
    fun getHealth(context: IContext<LivingEntity>): Any {
        val geoInstance = context.geoInstance
        if (geoInstance is PlayerCapability) {
            if (!geoInstance.isLocalPlayerModel) return geoInstance.positionTracker.health
        }
        return context.entity.health
    }

    @JvmStatic
    fun getMaxHealth(context: IContext<LivingEntity>): Any {
        val geoInstance = context.geoInstance
        if (geoInstance is PlayerCapability) {
            if (!geoInstance.isLocalPlayerModel) return geoInstance.positionTracker.maxHealth
        }
        return context.entity.maxHealth
    }

    @JvmStatic
    fun hasCape(abstractClientPlayer: AbstractClientPlayer): Boolean =
        !abstractClientPlayer.isInvisible && abstractClientPlayer.isModelPartShown(PlayerModelPart.CAPE) && abstractClientPlayer.skin.cape != null

    @JvmStatic
    fun getEquipmentCount(entity: LivingEntity): Int {
        var i = 0
        for (equipmentSlot in EquipmentSlot.entries) {
            if (equipmentSlot.isArmor && !CosmeticArmorHelper.getArmorItem(entity, equipmentSlot).isEmpty) {
                i++
            }
        }
        return i
    }

    @JvmStatic
    fun getItemMaxUseDuration(entity: LivingEntity): Int {
        val useItem: ItemStack = entity.useItem
        if (useItem.isEmpty) return 0
        return useItem.getUseDuration(entity)
    }

    @JvmStatic
    fun getYawSpeed(context: IContext<Entity>): Float {
        if (context.entity is LocalPlayer) return PlayerEntityFrameState.headYawDelta
        return 20.0f * (context.entity.yRot - context.entity.yRotO)
    }

    @JvmStatic
    fun getGroundSpeed(entity: Entity): Float {
        val deltaMovement: Vec3 = entity.deltaMovement
        return 20.0f * Mth.sqrt((deltaMovement.x * deltaMovement.x + deltaMovement.z * deltaMovement.z).toFloat())
    }

    @JvmStatic
    fun getVerticalSpeed(context: IContext<Entity>): Float {
        val positionTracker: EntityFrameStateTracker<*> = context.geoInstance.positionTracker
        return 20.0f * positionTracker.getPositionDelta().y.toFloat() / positionTracker.getTimeDelta()
    }

    @JvmStatic
    fun getCapeFlapAmount(context: IContext<Player>): Float {
        val gameTime: Float = context.animationEvent.frameTime
        val player: Player = context.entity
        val ap: AbstractClientPlayer = player as AbstractClientPlayer
        val fLerp: Float = (ap.avatarState().getInterpolatedCloakX(gameTime) - Mth.lerp(
            gameTime.toDouble(),
            player.xo,
            player.x
        )).toFloat()
        val fLerp2: Float = (ap.avatarState().getInterpolatedCloakY(gameTime) - Mth.lerp(
            gameTime.toDouble(),
            player.yo,
            player.y
        )).toFloat()
        val fLerp3: Float = (ap.avatarState().getInterpolatedCloakZ(gameTime) - Mth.lerp(
            gameTime.toDouble(),
            player.zo,
            player.z
        )).toFloat()
        val f: Float = player.yBodyRotO + (player.yBodyRot - player.yBodyRotO)
        val fSin: Float = Mth.sin((f * 0.017453292f).toDouble())
        val f2: Float = -Mth.cos((f * 0.017453292f).toDouble())
        val fClamp: Float = Mth.clamp(fLerp2 * 10.0f, -6.0f, 32.0f)
        var fClamp2: Float = Mth.clamp((fLerp * fSin + fLerp3 * f2) * 100.0f, 0.0f, 150.0f)
        if (fClamp2 < 0.0f) {
            fClamp2 = 0.0f
        }
        var fSin2: Float = fClamp + Mth.sin(
            (ap.avatarState().getInterpolatedWalkDistance(gameTime) * 6.0f).toDouble()
        ) * 32.0f * ap.avatarState().getInterpolatedBob(gameTime)
        if (player.isCrouching) {
            fSin2 += 25.0f
        }
        return Mth.clamp((6.0f + fClamp2 / 2.0f + fSin2) / 108.0f, 0.0f, 1.0f)
    }
}