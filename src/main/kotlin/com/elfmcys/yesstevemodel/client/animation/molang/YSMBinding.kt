package com.elfmcys.yesstevemodel.client.animation.molang

import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm.*
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ContextBinding
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.IValueEvaluator
import com.elfmcys.yesstevemodel.geckolib3.util.MathInterpolation
import com.elfmcys.yesstevemodel.util.CameraUtil
import com.elfmcys.yesstevemodel.util.accessors.ProjectileStateAccessor
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentUtils
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.FishingHook
import net.minecraft.world.entity.projectile.arrow.Arrow
import net.minecraft.world.entity.projectile.arrow.SpectralArrow
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile
import net.minecraft.world.item.CrossbowItem
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.level.LightLayer
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import rip.ysm.api.attribute.ForgeAttributes
import rip.ysm.compat.cosmeticarmorreworked.CosmeticArmorHelper
import rip.ysm.compat.curios.CuriosCompat
import rip.ysm.compat.touhoulittlemaid.TouhouLittleMaidCompat
import java.util.*
import kotlin.math.abs

object YSMBinding : ContextBinding() {
    init {
        function("dump_equipped_item", DumpEquippedItem())
        function("dump_relative_block", DumpRelativeBlock())
        `var`("dump_mods", IValueEvaluator { dumpMods(it) })
        entityVar("dump_effects", IValueEvaluator { dumpEffects(it) })
        entityVar("dump_biome", IValueEvaluator { dumpBiome(it) })
        function("mod_version", ModVersion())
        function("equipped_enchantment_level", EquippedEnchantmentLevel())
        function("effect_level", EffectLevel())
        function("relative_block_name", RelativeBlockName())
        function("relative_block_name_any", RelativeBlockNameAny())
        function("bone_rot", BoneRotation())
        function("bone_pos", BonePosition())
        function("bone_scale", BoneScale())
        function("bone_pivot_abs", BonePivotAbs())
        `var`("head_yaw", IValueEvaluator { it.data().netHeadYaw })
        `var`("head_pitch", IValueEvaluator { it.data().headPitch })
        `var`("weather", IValueEvaluator { getWeather(it.level()) })
        `var`(
            "dimension_name",
            IValueEvaluator {
                it.level()?.dimension()?.identifier()?.toString() ?: StringPool.EMPTY
            })
        `var`("fps", IValueEvaluator { Minecraft.getInstance().fps.toFloat() })
        `var`(
            "time_delta",
            IValueEvaluator { it.geoInstance().positionTracker.timeDelta / 20.0f })
        entityVar("ground_speed2", IValueEvaluator { getGroundSpeed2(it) })
        entityVar(
            "input_vertical",
            IValueEvaluator { MathInterpolation.getYawInterpolation(it) })
        entityVar(
            "input_horizontal",
            IValueEvaluator { MathInterpolation.getPitchInterpolation(it) })
        entityVar("person_view", IValueEvaluator { CameraUtil.getCameraType(it) })
        entityVar("rendering_in_paperdoll", IValueEvaluator { ModelPreviewRenderer.isExtraPlayer() })
        entityVar("rendering_in_inventory", IValueEvaluator { CameraUtil.isThirdPerson(it) })
        entityVar(
            "block_light",
            IValueEvaluator {
                it.level()?.getBrightness(LightLayer.BLOCK, it.entity().blockPosition()) ?: 0
            })
        entityVar(
            "sky_light",
            IValueEvaluator { it.level()?.getBrightness(LightLayer.SKY, it.entity().blockPosition()) ?: 0 })
        entityVar("is_passenger", IValueEvaluator { it.entity().isPassenger })
        entityVar("is_sleep", IValueEvaluator { it.entity().pose == Pose.SLEEPING })
        entityVar("is_sneak", IValueEvaluator { it.entity().onGround() && it.entity().pose == Pose.CROUCHING })
        entityVar("biome_category", IValueEvaluator { getBiomeCategory(it.entity()) })
        entityVar("is_open_air", IValueEvaluator { isOpenAir(it.entity()) })
        entityVar("eye_in_water", IValueEvaluator { it.entity().isUnderWater })
        entityVar("frozen_ticks", IValueEvaluator { it.entity().ticksFrozen })
        entityVar("air_supply", IValueEvaluator { it.entity().airSupply })
        entityVar("delta_movement_length", IValueEvaluator { it.entity().deltaMovement.length().toFloat() })
        livingEntityVar("has_helmet", IValueEvaluator { hasEquipment(it.entity(), EquipmentSlot.HEAD) })
        livingEntityVar("has_chest_plate", IValueEvaluator { hasEquipment(it.entity(), EquipmentSlot.CHEST) })
        livingEntityVar("has_leggings", IValueEvaluator { hasEquipment(it.entity(), EquipmentSlot.LEGS) })
        livingEntityVar("has_boots", IValueEvaluator { hasEquipment(it.entity(), EquipmentSlot.FEET) })
        livingEntityVar("has_mainhand", IValueEvaluator { hasEquipment(it.entity(), EquipmentSlot.MAINHAND) })
        livingEntityVar("has_offhand", IValueEvaluator { hasEquipment(it.entity(), EquipmentSlot.OFFHAND) })
        livingEntityVar("has_elytra", IValueEvaluator { !CosmeticArmorHelper.getElytraItem(it.entity()).isEmpty })
        livingEntityVar("is_riptide", IValueEvaluator { it.entity().isAutoSpinAttack })
        livingEntityVar("armor_value", IValueEvaluator { it.entity().armorValue })
        livingEntityVar("hurt_time", IValueEvaluator { it.entity().hurtTime })
        livingEntityVar("is_close_eyes", IValueEvaluator { isCloseEyes(it.animationEvent(), it.entity()) })
        livingEntityVar("on_ladder", IValueEvaluator { it.entity().onClimbable() })
        livingEntityVar("ladder_facing", LadderFacing())
        livingEntityVar("arrow_count", IValueEvaluator { it.entity().arrowCount })
        livingEntityVar("stinger_count", IValueEvaluator { it.entity().stingerCount })
        livingEntityVar("entity_type", IValueEvaluator { getEntityTypeName(it) })
        livingEntityVar("is_player", IValueEvaluator { "player" == getEntityTypeName(it) })
        livingEntityVar("is_maid", IValueEvaluator { "maid" == getEntityTypeName(it) })
        livingEntityVar("food_level", IValueEvaluator { getFoodLevel(it) })
        livingEntityVar("xxa", IValueEvaluator { getXxa(it) })
        livingEntityVar("yya", IValueEvaluator { getYya(it) })
        livingEntityVar("zza", IValueEvaluator { getZza(it) })
        livingEntityVar(
            "mainhand_charged_crossbow",
            IValueEvaluator { isChargedCrossbow(it, InteractionHand.MAIN_HAND) })
        livingEntityVar(
            "offhand_charged_crossbow",
            IValueEvaluator { isChargedCrossbow(it, InteractionHand.OFF_HAND) })
        livingEntityVar("is_fishing", IValueEvaluator { isFishing(it) })
        livingEntityVar("swinging", IValueEvaluator { it.entity().swinging })
        livingEntityVar("swing_time", IValueEvaluator { it.entity().swingTime })
        livingEntityVar(
            "swinging_arm",
            IValueEvaluator { if (it.entity().swingingArm == InteractionHand.MAIN_HAND) 0 else 1 })
        livingEntityVar(
            "attack_time",
            IValueEvaluator {
                it.entity().getAttackAnim(it.animationEvent().frameTime)
            })
        playerEntityVar("texture_name", TextureName())
        playerEntityVar("first_person_mod_hide", FirstPersonModHide())
        playerEntityVar(
            "has_left_shoulder_parrot",
            IValueEvaluator { hasShoulderParrot(it.entity(), true) })
        playerEntityVar(
            "has_right_shoulder_parrot",
            IValueEvaluator { hasShoulderParrot(it.entity(), false) })
        playerEntityVar(
            "left_shoulder_parrot_variant",
            IValueEvaluator { getShoulderParrotVariant(it.entity(), true) })
        playerEntityVar(
            "right_shoulder_parrot_variant",
            IValueEvaluator { getShoulderParrotVariant(it.entity(), false) })
        playerEntityVar(
            "attack_damage",
            IValueEvaluator { it.entity().getAttributeValue(Attributes.ATTACK_DAMAGE) })
        playerEntityVar(
            "attack_speed",
            IValueEvaluator { it.entity().getAttributeValue(Attributes.ATTACK_SPEED) })
        playerEntityVar(
            "attack_knockback",
            IValueEvaluator { it.entity().getAttributeValue(Attributes.ATTACK_KNOCKBACK) })
        playerEntityVar(
            "movement_speed",
            IValueEvaluator { it.entity().getAttributeValue(Attributes.MOVEMENT_SPEED) })
        playerEntityVar(
            "knockback_resistance",
            IValueEvaluator {
                it.entity().getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)
            })
        playerEntityVar(
            "luck",
            IValueEvaluator { it.entity().getAttributeValue(Attributes.LUCK) })
        playerEntityVar(
            "block_reach",
            IValueEvaluator {
                ForgeAttributes.getValue(
                    it.entity(),
                    ForgeAttributes.blockReach(),
                    4.5
                )
            })
        playerEntityVar(
            "entity_reach",
            IValueEvaluator {
                ForgeAttributes.getValue(
                    it.entity(),
                    ForgeAttributes.entityReach(),
                    3.0
                )
            })
        playerEntityVar(
            "swim_speed",
            IValueEvaluator {
                ForgeAttributes.getValue(
                    it.entity(),
                    ForgeAttributes.swimSpeed(),
                    1.0
                )
            })
        playerEntityVar(
            "entity_gravity",
            IValueEvaluator {
                ForgeAttributes.getValue(
                    it.entity(),
                    ForgeAttributes.entityGravity(),
                    0.08
                )
            })
        playerEntityVar(
            "step_height_addition",
            IValueEvaluator {
                ForgeAttributes.getValue(
                    it.entity(),
                    ForgeAttributes.stepHeightAddition(),
                    0.0
                )
            })
        playerEntityVar(
            "nametag_distance",
            IValueEvaluator {
                ForgeAttributes.getValue(
                    it.entity(),
                    ForgeAttributes.nametagDistance(),
                    64.0
                )
            })
        playerEntityVar(
            "in_shield_block_cooldown",
            IValueEvaluator { isInShieldBlockCooldown(it) })
        clientPlayerEntityVar(
            "elytra_rot_x",
            IValueEvaluator {
                Math.toDegrees(
                    it.entity().elytraAnimationState.getRotX(it.animationEvent().frameTime).toDouble()
                )
            })
        clientPlayerEntityVar(
            "elytra_rot_y",
            IValueEvaluator {
                Math.toDegrees(
                    it.entity().elytraAnimationState.getRotY(it.animationEvent().frameTime).toDouble()
                )
            })
        clientPlayerEntityVar(
            "elytra_rot_z",
            IValueEvaluator {
                Math.toDegrees(
                    it.entity().elytraAnimationState.getRotZ(it.animationEvent().frameTime).toDouble()
                )
            })
        localPlayerEntityVar("hit_target_id", IValueEvaluator { getHitTargetId(it) })
        localPlayerEntityVar("hit_target_type", IValueEvaluator { getHitTargetType(it) })
        function("first_order", FirstOrderFunction())
        function("second_order", SecondOrderFunction())
        function("particle", Particle(false))
        function("abs_particle", Particle(true))
        function("perlin_noise", PerlinNoise())
        function("play_sound", SoundFunction.PlaySoundFunction())
        function("stop_sound", SoundFunction.StopSoundFunction())
        function("stop_all_sounds", SoundFunction.StopAllSoundsFunction())
        function("keyboard", InputKeyDetectionFunction.Keyboard())
        function("mouse", InputKeyDetectionFunction.Mouse())
        function(MolangEventDispatcher.SYNC, Sync())
        function(MolangEventDispatcher.DEFER, Defer())
        projectileEntityVar("projectile_owner", IValueEvaluator { it.createChild(it.entity().owner) })
        throwableProjectileEntityVar(
            "throwable_item",
            IValueEvaluator { getThrowableItemId(it) })
        fishHookEntityVar("hooked_in", IValueEvaluator { getHookedEntityType(it) })
        fishHookEntityVar(
            "is_biting",
            IValueEvaluator { it.entity().biting })
        abstractArrowEntityVar(
            "on_ground_time",
            IValueEvaluator { (it.entity() as ProjectileStateAccessor).`ysm$getInGroundTime`() })
        abstractArrowEntityVar(
            "in_ground",
            IValueEvaluator { (it.entity() as ProjectileStateAccessor).`ysm$isArrowInGround`() })
        abstractArrowEntityVar(
            "is_spectral_arrow",
            IValueEvaluator { it.entity() is SpectralArrow })
        abstractArrowEntityVar(
            "shoot_item_id",
            IValueEvaluator { (it.entity() as ProjectileStateAccessor).`ysm$getOwnerItemId`() })
        CuriosCompat.registerCuriosItems(this)
    }

    @JvmStatic
    fun getHitTargetId(context: IContext<LocalPlayer>): String {
        val hitResult = Minecraft.getInstance().hitResult
        if (hitResult is BlockHitResult) {
            val clientLevel = Minecraft.getInstance().level ?: return StringPool.EMPTY
            if (hitResult.type == HitResult.Type.MISS) {
                return StringPool.EMPTY
            }
            val key = BuiltInRegistries.BLOCK.getKey(clientLevel.getBlockState(hitResult.blockPos).block)
            return key.toString()
        }
        if (hitResult is EntityHitResult) {
            val key2 = BuiltInRegistries.ENTITY_TYPE.getKey(hitResult.entity.type)
            return key2.toString()
        }
        return StringPool.EMPTY
    }

    @JvmStatic
    fun getHitTargetType(context: IContext<LocalPlayer>): String {
        val hitResult = Minecraft.getInstance().hitResult ?: return StringPool.EMPTY
        return when (hitResult.type) {
            HitResult.Type.BLOCK -> "block"
            HitResult.Type.ENTITY -> "entity"
            else -> StringPool.EMPTY
        }
    }

    @JvmStatic
    fun getHookedEntityType(context: IContext<FishingHook>): String {
        val entity = context.entity().hookedIn
        if (entity != null) {
            val key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.type)
            return key.toString()
        }
        return StringPool.EMPTY
    }

    @JvmStatic
    fun getThrowableItemId(context: IContext<ThrowableItemProjectile>): String {
        val projectile = context.entity()
        val key = BuiltInRegistries.ITEM.getKey(projectile.defaultItem)
        return key.toString()
    }

    @JvmStatic
    fun getGroundSpeed2(context: IContext<Entity>): Float {
        val tracker = context.geoInstance().positionTracker
        val delta = tracker.positionDelta
        return 20.0f * Mth.sqrt((delta.x * delta.x + delta.z * delta.z).toFloat()) / tracker.timeDelta
    }

    @JvmStatic
    fun getXxa(context: IContext<LivingEntity>): Float {
        val animatable = context.geoInstance()
        if (animatable is PlayerCapability) {
            if (!animatable.isLocalPlayerModel) {
                return animatable.positionTracker.strafeInput
            }
        }
        return context.entity().xxa
    }

    @JvmStatic
    fun getYya(context: IContext<LivingEntity>): Float {
        val animatable = context.geoInstance()
        if (animatable is PlayerCapability) {
            if (!animatable.isLocalPlayerModel) {
                return animatable.positionTracker.verticalInput
            }
        }
        return context.entity().yya
    }

    @JvmStatic
    fun getZza(context: IContext<LivingEntity>): Float {
        val animatable = context.geoInstance()
        if (animatable is PlayerCapability) {
            if (!animatable.isLocalPlayerModel) {
                return animatable.positionTracker.forwardInput
            }
        }
        return context.entity().zza
    }

    @JvmStatic
    fun isInShieldBlockCooldown(context: IContext<Player>): Boolean {
        val animatable = context.geoInstance()
        return animatable is PlayerCapability && animatable.positionTracker.isShieldBlocking
    }

    @JvmStatic
    fun isFishing(context: IContext<LivingEntity>): Boolean {
        val livingEntity = context.entity()
        if (livingEntity is Player) {
            return livingEntity.fishing != null
        }
        return TouhouLittleMaidCompat.isMaidSitting(livingEntity)
    }

    @JvmStatic
    fun isChargedCrossbow(context: IContext<LivingEntity>, interactionHand: InteractionHand): Boolean {
        val itemInHand = context.entity().getItemInHand(interactionHand)
        return itemInHand.`is`(Items.CROSSBOW) && CrossbowItem.isCharged(itemInHand)
    }

    @JvmStatic
    fun getEntityTypeName(context: IContext<LivingEntity>): String {
        val livingEntity = context.entity()
        if (livingEntity is Player) return "player"
        val key = BuiltInRegistries.ENTITY_TYPE.getKey(livingEntity.type)
        if ("touhou_little_maid" == key.namespace && "maid" == key.path) return "maid"
        return key.toString()
    }

    @JvmStatic
    fun getFoodLevel(context: IContext<LivingEntity>): Any {
        val animatable = context.geoInstance()
        if (animatable is PlayerCapability && !animatable.isLocalPlayerModel) return animatable.positionTracker.foodLevel
        val livingEntity = context.entity()
        if (livingEntity is Player) return livingEntity.foodData.foodLevel
        return 20
    }

    @JvmStatic
    fun isCloseEyes(event: AnimationEvent<*>, livingEntity: LivingEntity): Boolean {
        val blinkPhase = (event.currentTick + abs(livingEntity.uuid.leastSignificantBits) % 10) % 90.0f
        return livingEntity.isSleeping || blinkPhase in 85.0f..90.0f
    }

    @JvmStatic
    fun hasEquipment(livingEntity: LivingEntity, equipmentSlot: EquipmentSlot): Boolean =
        !CosmeticArmorHelper.getArmorItem(livingEntity, equipmentSlot).isEmpty

    @JvmStatic
    fun getWeather(clientLevel: ClientLevel?): Int = when {
        clientLevel == null -> 0
        clientLevel.isThundering -> 2
        clientLevel.isRaining -> 1
        else -> 0
    }

    // TODO: What
    @Deprecated("")
    @JvmStatic
    fun getBiomeCategory(entity: Entity): String? {
        return null
    }

    // TODO: Always null don't know why
    @JvmStatic
    fun dumpMods(context: IContext<*>): Any? {
        if (!context.isDebugMode()) return null
        FabricLoader.getInstance().allMods.sortedBy { it.metadata.name }.forEach { mod ->
            context.logWarningComponent(
                Component.literal("Mod: display ")
                    .append(ComponentUtils.copyOnClickText(mod.metadata.name))
                    .append(Component.literal("  id "))
                    .append(ComponentUtils.copyOnClickText(mod.metadata.id))
            )
        }
        return null
    }

    @JvmStatic
    fun dumpEffects(context: IContext<Entity>): Any? {
        if (!context.isDebugMode()) return null
        val activeEffects = when (val entity = context.entity()) {
            is Arrow -> {
                val potionContents =
                    entity.pickupItemStackOrigin.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
                potionContents.allEffects.toList()
            }

            is LivingEntity -> entity.activeEffects
            else -> return null
        }
        for (mobEffectInstance in activeEffects) {
            context.logWarningComponent(
                Component.literal("Effect: display ")
                    .append(ComponentUtils.copyOnClickText(mobEffectInstance.effect.value().displayName.getString(99)))
                    .append(Component.literal("  name "))
                    .append(
                        ComponentUtils.copyOnClickText(
                            BuiltInRegistries.MOB_EFFECT.getKey(mobEffectInstance.effect.value()).toString()
                        )
                    )
                    .append("  lv=")
                    .append((mobEffectInstance.amplifier + 1).toString())
            )
        }
        return null
    }

    @JvmStatic
    fun dumpBiome(context: IContext<Entity>): Any? {
        if (!context.isDebugMode()) {
            return null
        }
        val biome = context.entity().level().getBiome(context.entity().blockPosition())
        biome.unwrapKey().ifPresent { resourceKey ->
            context.logWarningComponent(
                Component.literal("Name ")
                    .append(ComponentUtils.copyOnClickText(resourceKey.identifier().toString()))
            )
        }
        biome.tags().forEach { tagKey ->
            context.logWarningComponent(
                Component.literal("Tag ").append(ComponentUtils.copyOnClickText(tagKey.location().toString()))
            )
        }
        return null
    }

    @JvmStatic
    fun isOpenAir(entity: Entity): Boolean {
        val blockPos = entity.blockPosition()
        return entity.level().canSeeSky(blockPos) && entity.level()
            .getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).y <= blockPos.y
    }

    @JvmStatic
    fun getShoulderParrotVariant(player: Player, leftShoulder: Boolean): String {
        if (player !is AbstractClientPlayer) return "empty"
        val variant = player.getParrotVariantOnShoulder(leftShoulder)
        return variant?.name?.lowercase(Locale.ENGLISH) ?: "empty"
    }

    @JvmStatic
    fun hasShoulderParrot(player: Player, leftShoulder: Boolean): Boolean =
        player is AbstractClientPlayer && player.getParrotVariantOnShoulder(leftShoulder) != null
}