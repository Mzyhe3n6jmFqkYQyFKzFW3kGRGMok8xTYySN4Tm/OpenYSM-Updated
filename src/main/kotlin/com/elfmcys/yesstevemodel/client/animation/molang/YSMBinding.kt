package com.elfmcys.yesstevemodel.client.animation.molang

import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm.*
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.EntityFrameStateTracker
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ContextBinding
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.IValueEvaluator
import com.elfmcys.yesstevemodel.geckolib3.util.MathInterpolation
import com.elfmcys.yesstevemodel.mixin.client.ThrowableItemProjectileAccessor
import com.elfmcys.yesstevemodel.util.CameraUtil
import com.elfmcys.yesstevemodel.util.accessors.ProjectileStateAccessor
import com.elfmcys.yesstevemodel.util.data.LazySupplier
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentUtils
import net.minecraft.resources.Identifier
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.animal.parrot.Parrot
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.FishingHook
import net.minecraft.world.entity.projectile.arrow.AbstractArrow
import net.minecraft.world.entity.projectile.arrow.Arrow
import net.minecraft.world.entity.projectile.arrow.SpectralArrow
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile
import net.minecraft.world.item.CrossbowItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.level.LightLayer
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import rip.ysm.api.attribute.ForgeAttributes
import rip.ysm.compat.cosmeticarmorreworked.CosmeticArmorHelper
import rip.ysm.compat.curios.CuriosCompat
import rip.ysm.compat.touhoulittlemaid.TouhouLittleMaidCompat
import java.util.*
import kotlin.math.abs

class YSMBinding private constructor() : ContextBinding() {
    init {
        function("dump_equipped_item", DumpEquippedItem())
        function("dump_relative_block", DumpRelativeBlock())
        `var`("dump_mods", IValueEvaluator { ctx -> dumpMods(ctx) })
        entityVar("dump_effects", IValueEvaluator { ctx -> dumpEffects(ctx) })
        entityVar("dump_biome", IValueEvaluator { ctx -> dumpBiome(ctx) })
        function("mod_version", ModVersion())
        function("equipped_enchantment_level", EquippedEnchantmentLevel())
        function("effect_level", EffectLevel())
        function("relative_block_name", RelativeBlockName())
        function("relative_block_name_any", RelativeBlockNameAny())
        function("bone_rot", BoneRotation())
        function("bone_pos", BonePosition())
        function("bone_scale", BoneScale())
        function("bone_pivot_abs", BonePivotAbs())
        `var`("head_yaw", IValueEvaluator { ctx: IContext<Any> -> ctx.data().netHeadYaw })
        `var`("head_pitch", IValueEvaluator { ctx: IContext<Any> -> ctx.data().headPitch })
        `var`("weather", IValueEvaluator { ctx: IContext<Any> -> getWeather(ctx.level()) })
        `var`(
            "dimension_name",
            IValueEvaluator { ctx: IContext<Any> ->
                ctx.level()?.dimension()?.identifier()?.toString() ?: StringPool.EMPTY
            })
        `var`("fps", IValueEvaluator { _ -> Minecraft.getInstance().fps.toFloat() })
        `var`(
            "time_delta",
            IValueEvaluator { ctx: IContext<Any> -> ctx.geoInstance().positionTracker.timeDelta / 20.0f })
        entityVar("ground_speed2", IValueEvaluator { ctx: IContext<Entity> -> getGroundSpeed2(ctx) })
        entityVar(
            "input_vertical",
            IValueEvaluator { ctx: IContext<Entity> -> MathInterpolation.getYawInterpolation(ctx) })
        entityVar(
            "input_horizontal",
            IValueEvaluator { ctx: IContext<Entity> -> MathInterpolation.getPitchInterpolation(ctx) })
        entityVar("person_view", IValueEvaluator { ctx: IContext<Entity> -> CameraUtil.getCameraType(ctx) })
        entityVar("rendering_in_paperdoll", IValueEvaluator { _ -> ModelPreviewRenderer.isExtraPlayer() })
        entityVar("rendering_in_inventory", IValueEvaluator { ctx: IContext<Entity> -> CameraUtil.isThirdPerson(ctx) })
        entityVar(
            "block_light",
            IValueEvaluator { ctx: IContext<Entity> ->
                ctx.level()?.getBrightness(LightLayer.BLOCK, ctx.entity().blockPosition()) ?: 0
            })
        entityVar(
            "sky_light",
            IValueEvaluator { ctx: IContext<Entity> ->
                ctx.level()?.getBrightness(LightLayer.SKY, ctx.entity().blockPosition()) ?: 0
            })
        entityVar("is_passenger", IValueEvaluator { ctx: IContext<Entity> -> ctx.entity().isPassenger })
        entityVar("is_sleep", IValueEvaluator { ctx: IContext<Entity> -> ctx.entity().pose == Pose.SLEEPING })
        entityVar(
            "is_sneak",
            IValueEvaluator { ctx: IContext<Entity> -> ctx.entity().onGround() && ctx.entity().pose == Pose.CROUCHING })
        entityVar("biome_category", IValueEvaluator { ctx: IContext<Entity> -> getBiomeCategory(ctx.entity()) })
        entityVar("is_open_air", IValueEvaluator { ctx: IContext<Entity> -> isOpenAir(ctx.entity()) })
        entityVar("eye_in_water", IValueEvaluator { ctx: IContext<Entity> -> ctx.entity().isUnderWater })
        entityVar("frozen_ticks", IValueEvaluator { ctx: IContext<Entity> -> ctx.entity().ticksFrozen })
        entityVar("air_supply", IValueEvaluator { ctx: IContext<Entity> -> ctx.entity().airSupply })
        entityVar(
            "delta_movement_length",
            IValueEvaluator { ctx: IContext<Entity> -> ctx.entity().deltaMovement.length().toFloat() })
        livingEntityVar(
            "has_helmet",
            IValueEvaluator { ctx: IContext<LivingEntity> -> hasEquipment(ctx.entity(), EquipmentSlot.HEAD) })
        livingEntityVar(
            "has_chest_plate",
            IValueEvaluator { ctx: IContext<LivingEntity> -> hasEquipment(ctx.entity(), EquipmentSlot.CHEST) })
        livingEntityVar(
            "has_leggings",
            IValueEvaluator { ctx: IContext<LivingEntity> -> hasEquipment(ctx.entity(), EquipmentSlot.LEGS) })
        livingEntityVar(
            "has_boots",
            IValueEvaluator { ctx: IContext<LivingEntity> -> hasEquipment(ctx.entity(), EquipmentSlot.FEET) })
        livingEntityVar(
            "has_mainhand",
            IValueEvaluator { ctx: IContext<LivingEntity> -> hasEquipment(ctx.entity(), EquipmentSlot.MAINHAND) })
        livingEntityVar(
            "has_offhand",
            IValueEvaluator { ctx: IContext<LivingEntity> -> hasEquipment(ctx.entity(), EquipmentSlot.OFFHAND) })
        livingEntityVar(
            "has_elytra",
            IValueEvaluator { ctx: IContext<LivingEntity> -> !CosmeticArmorHelper.getElytraItem(ctx.entity()).isEmpty })
        livingEntityVar("is_riptide", IValueEvaluator { ctx: IContext<LivingEntity> -> ctx.entity().isAutoSpinAttack })
        livingEntityVar("armor_value", IValueEvaluator { ctx: IContext<LivingEntity> -> ctx.entity().armorValue })
        livingEntityVar("hurt_time", IValueEvaluator { ctx: IContext<LivingEntity> -> ctx.entity().hurtTime })
        livingEntityVar(
            "is_close_eyes",
            IValueEvaluator { ctx: IContext<LivingEntity> -> isCloseEyes(ctx.animationEvent(), ctx.entity()) })
        livingEntityVar("on_ladder", IValueEvaluator { ctx: IContext<LivingEntity> -> ctx.entity().onClimbable() })
        livingEntityVar("ladder_facing", LadderFacing())
        livingEntityVar("arrow_count", IValueEvaluator { ctx: IContext<LivingEntity> -> ctx.entity().arrowCount })
        livingEntityVar("stinger_count", IValueEvaluator { ctx: IContext<LivingEntity> -> ctx.entity().stingerCount })
        livingEntityVar("entity_type", IValueEvaluator { ctx: IContext<LivingEntity> -> getEntityTypeName(ctx) })
        livingEntityVar(
            "is_player",
            IValueEvaluator { ctx: IContext<LivingEntity> -> "player" == getEntityTypeName(ctx) })
        livingEntityVar("is_maid", IValueEvaluator { ctx: IContext<LivingEntity> -> "maid" == getEntityTypeName(ctx) })
        livingEntityVar("food_level", IValueEvaluator { ctx: IContext<LivingEntity> -> getFoodLevel(ctx) })
        livingEntityVar("xxa", IValueEvaluator { ctx: IContext<LivingEntity> -> getXxa(ctx) })
        livingEntityVar("yya", IValueEvaluator { ctx: IContext<LivingEntity> -> getYya(ctx) })
        livingEntityVar("zza", IValueEvaluator { ctx: IContext<LivingEntity> -> getZza(ctx) })
        livingEntityVar(
            "mainhand_charged_crossbow",
            IValueEvaluator { ctx: IContext<LivingEntity> -> isChargedCrossbow(ctx, InteractionHand.MAIN_HAND) })
        livingEntityVar(
            "offhand_charged_crossbow",
            IValueEvaluator { ctx: IContext<LivingEntity> -> isChargedCrossbow(ctx, InteractionHand.OFF_HAND) })
        livingEntityVar("is_fishing", IValueEvaluator { ctx: IContext<LivingEntity> -> isFishing(ctx) })
        livingEntityVar("swinging", IValueEvaluator { ctx: IContext<LivingEntity> -> ctx.entity().swinging })
        livingEntityVar("swing_time", IValueEvaluator { ctx: IContext<LivingEntity> -> ctx.entity().swingTime })
        livingEntityVar(
            "swinging_arm",
            IValueEvaluator { ctx: IContext<LivingEntity> -> if (ctx.entity().swingingArm == InteractionHand.MAIN_HAND) 0 else 1 })
        livingEntityVar(
            "attack_time",
            IValueEvaluator { ctx: IContext<LivingEntity> ->
                ctx.entity().getAttackAnim(ctx.animationEvent().frameTime)
            })
        playerEntityVar("texture_name", TextureName())
        playerEntityVar("first_person_mod_hide", FirstPersonModHide())
        playerEntityVar(
            "has_left_shoulder_parrot",
            IValueEvaluator { ctx: IContext<Player> -> hasShoulderParrot(ctx.entity(), true) })
        playerEntityVar(
            "has_right_shoulder_parrot",
            IValueEvaluator { ctx: IContext<Player> -> hasShoulderParrot(ctx.entity(), false) })
        playerEntityVar(
            "left_shoulder_parrot_variant",
            IValueEvaluator { ctx: IContext<Player> -> getShoulderParrotVariant(ctx.entity(), true) })
        playerEntityVar(
            "right_shoulder_parrot_variant",
            IValueEvaluator { ctx: IContext<Player> -> getShoulderParrotVariant(ctx.entity(), false) })
        playerEntityVar(
            "attack_damage",
            IValueEvaluator { ctx: IContext<Player> -> ctx.entity().getAttributeValue(Attributes.ATTACK_DAMAGE) })
        playerEntityVar(
            "attack_speed",
            IValueEvaluator { ctx: IContext<Player> -> ctx.entity().getAttributeValue(Attributes.ATTACK_SPEED) })
        playerEntityVar(
            "attack_knockback",
            IValueEvaluator { ctx: IContext<Player> -> ctx.entity().getAttributeValue(Attributes.ATTACK_KNOCKBACK) })
        playerEntityVar(
            "movement_speed",
            IValueEvaluator { ctx: IContext<Player> -> ctx.entity().getAttributeValue(Attributes.MOVEMENT_SPEED) })
        playerEntityVar(
            "knockback_resistance",
            IValueEvaluator { ctx: IContext<Player> ->
                ctx.entity().getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)
            })
        playerEntityVar(
            "luck",
            IValueEvaluator { ctx: IContext<Player> -> ctx.entity().getAttributeValue(Attributes.LUCK) })
        playerEntityVar(
            "block_reach",
            IValueEvaluator { ctx: IContext<Player> ->
                ForgeAttributes.getValue(
                    ctx.entity(),
                    ForgeAttributes.blockReach(),
                    4.5
                )
            })
        playerEntityVar(
            "entity_reach",
            IValueEvaluator { ctx: IContext<Player> ->
                ForgeAttributes.getValue(
                    ctx.entity(),
                    ForgeAttributes.entityReach(),
                    3.0
                )
            })
        playerEntityVar(
            "swim_speed",
            IValueEvaluator { ctx: IContext<Player> ->
                ForgeAttributes.getValue(
                    ctx.entity(),
                    ForgeAttributes.swimSpeed(),
                    1.0
                )
            })
        playerEntityVar(
            "entity_gravity",
            IValueEvaluator { ctx: IContext<Player> ->
                ForgeAttributes.getValue(
                    ctx.entity(),
                    ForgeAttributes.entityGravity(),
                    0.08
                )
            })
        playerEntityVar(
            "step_height_addition",
            IValueEvaluator { ctx: IContext<Player> ->
                ForgeAttributes.getValue(
                    ctx.entity(),
                    ForgeAttributes.stepHeightAddition(),
                    0.0
                )
            })
        playerEntityVar(
            "nametag_distance",
            IValueEvaluator { ctx: IContext<Player> ->
                ForgeAttributes.getValue(
                    ctx.entity(),
                    ForgeAttributes.nametagDistance(),
                    64.0
                )
            })
        playerEntityVar(
            "in_shield_block_cooldown",
            IValueEvaluator { ctx: IContext<Player> -> isInShieldBlockCooldown(ctx) })
        clientPlayerEntityVar(
            "elytra_rot_x",
            IValueEvaluator { ctx: IContext<AbstractClientPlayer> ->
                Math.toDegrees(
                    ctx.entity().elytraAnimationState.getRotX(ctx.animationEvent().frameTime).toDouble()
                )
            })
        clientPlayerEntityVar(
            "elytra_rot_y",
            IValueEvaluator { ctx: IContext<AbstractClientPlayer> ->
                Math.toDegrees(
                    ctx.entity().elytraAnimationState.getRotY(ctx.animationEvent().frameTime).toDouble()
                )
            })
        clientPlayerEntityVar(
            "elytra_rot_z",
            IValueEvaluator { ctx: IContext<AbstractClientPlayer> ->
                Math.toDegrees(
                    ctx.entity().elytraAnimationState.getRotZ(ctx.animationEvent().frameTime).toDouble()
                )
            })
        localPlayerEntityVar("hit_target_id", IValueEvaluator { ctx: IContext<LocalPlayer> -> getHitTargetId(ctx) })
        localPlayerEntityVar("hit_target_type", IValueEvaluator { ctx: IContext<LocalPlayer> -> getHitTargetType(ctx) })
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
        projectileEntityVar("projectile_owner", IValueEvaluator { ctx -> ctx.createChild(ctx.entity().owner) })
        throwableProjectileEntityVar(
            "throwable_item",
            IValueEvaluator { ctx: IContext<ThrowableItemProjectile> -> getThrowableItemId(ctx) })
        fishHookEntityVar("hooked_in", IValueEvaluator { ctx: IContext<FishingHook> -> getHookedEntityType(ctx) })
        fishHookEntityVar(
            "is_biting",
            IValueEvaluator { ctx: IContext<FishingHook> -> ctx.entity().biting })
        abstractArrowEntityVar(
            "on_ground_time",
            IValueEvaluator { ctx: IContext<AbstractArrow> -> (ctx.entity() as ProjectileStateAccessor).`ysm$getInGroundTime`() })
        abstractArrowEntityVar(
            "in_ground",
            IValueEvaluator { ctx: IContext<AbstractArrow> -> (ctx.entity() as ProjectileStateAccessor).`ysm$isArrowInGround`() })
        abstractArrowEntityVar(
            "is_spectral_arrow",
            IValueEvaluator { ctx: IContext<AbstractArrow> -> ctx.entity() is SpectralArrow })
        abstractArrowEntityVar(
            "shoot_item_id",
            IValueEvaluator { ctx: IContext<AbstractArrow> -> (ctx.entity() as ProjectileStateAccessor).`ysm$getOwnerItemId`() })
        CuriosCompat.registerCuriosItems(this)
    }

    companion object {
        @JvmField
        val INSTANCE: LazySupplier<YSMBinding> = LazySupplier(::YSMBinding)

        @JvmStatic
        fun getHitTargetId(context: IContext<LocalPlayer>): String {
            val hitResult: HitResult? = Minecraft.getInstance().hitResult
            if (hitResult is BlockHitResult) {
                val clientLevel: ClientLevel = Minecraft.getInstance().level ?: return StringPool.EMPTY
                if (hitResult.type == HitResult.Type.MISS) {
                    return StringPool.EMPTY
                }
                val key: Identifier =
                    BuiltInRegistries.BLOCK.getKey(clientLevel.getBlockState(hitResult.blockPos).block)
                return key?.toString() ?: StringPool.EMPTY
            }
            if (hitResult is EntityHitResult) {
                val key2: Identifier = BuiltInRegistries.ENTITY_TYPE.getKey(hitResult.entity.type)
                return key2?.toString() ?: StringPool.EMPTY
            }
            return StringPool.EMPTY
        }

        @JvmStatic
        fun getHitTargetType(context: IContext<LocalPlayer>): String {
            val hitResult: HitResult = Minecraft.getInstance().hitResult ?: return StringPool.EMPTY
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
            if (projectile is ThrowableItemProjectileAccessor) {
                val key = BuiltInRegistries.ITEM.getKey(projectile.invokeGetDefaultItem())
                return key.toString()
            }
            return StringPool.EMPTY
        }

        @JvmStatic
        fun getGroundSpeed2(context: IContext<Entity>): Float {
            val tracker: EntityFrameStateTracker<*> = context.geoInstance().positionTracker
            val delta: Vec3 = tracker.positionDelta
            return (20.0f * Mth.sqrt((delta.x * delta.x + delta.z * delta.z).toFloat())) / tracker.timeDelta
        }

        @JvmStatic
        fun getXxa(context: IContext<LivingEntity>): Float {
            val animatable: AnimatableEntity<*> = context.geoInstance()
            if (animatable is PlayerCapability) {
                if (!animatable.isLocalPlayerModel()) {
                    return animatable.getPositionTracker().strafeInput
                }
            }
            return context.entity().xxa
        }

        @JvmStatic
        fun getYya(context: IContext<LivingEntity>): Float {
            val animatable: AnimatableEntity<*> = context.geoInstance()
            if (animatable is PlayerCapability) {
                if (!animatable.isLocalPlayerModel()) {
                    return animatable.getPositionTracker().verticalInput
                }
            }
            return context.entity().yya
        }

        @JvmStatic
        fun getZza(context: IContext<LivingEntity>): Float {
            val animatable: AnimatableEntity<*> = context.geoInstance()
            if (animatable is PlayerCapability) {
                if (!animatable.isLocalPlayerModel()) {
                    return animatable.getPositionTracker().forwardInput
                }
            }
            return context.entity().zza
        }

        @JvmStatic
        fun isInShieldBlockCooldown(context: IContext<Player>): Boolean {
            val animatable: AnimatableEntity<*> = context.geoInstance()
            return animatable is PlayerCapability && animatable.getPositionTracker().isShieldBlocking
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
            val itemInHand: ItemStack = context.entity().getItemInHand(interactionHand)
            return itemInHand.`is`(Items.CROSSBOW) && CrossbowItem.isCharged(itemInHand)
        }

        @JvmStatic
        fun getEntityTypeName(context: IContext<LivingEntity>): String {
            val livingEntity = context.entity()
            if (livingEntity is Player) {
                return "player"
            }
            val key: Identifier = BuiltInRegistries.ENTITY_TYPE.getKey(livingEntity.type)
            if ("touhou_little_maid" == key.namespace && "maid" == key.path) {
                return "maid"
            }
            return key.toString()
        }

        @JvmStatic
        fun getFoodLevel(context: IContext<LivingEntity>): Any {
            val animatable: AnimatableEntity<*> = context.geoInstance()
            if (animatable is PlayerCapability) {
                if (!animatable.isLocalPlayerModel()) return animatable.getPositionTracker().foodLevel
            }
            val livingEntity = context.entity()
            if (livingEntity is Player) return livingEntity.foodData.foodLevel
            return 20
        }

        @JvmStatic
        fun isCloseEyes(event: AnimationEvent<*>, livingEntity: LivingEntity): Boolean {
            val blinkPhase: Float = (event.currentTick + (abs(livingEntity.uuid.leastSignificantBits) % 10)) % 90.0f
            return livingEntity.isSleeping || (blinkPhase in 85.0f..90.0f)
        }

        @JvmStatic
        fun hasEquipment(livingEntity: LivingEntity, equipmentSlot: EquipmentSlot): Boolean {
            return !CosmeticArmorHelper.getArmorItem(livingEntity, equipmentSlot).isEmpty
        }

        @JvmStatic
        fun getWeather(clientLevel: ClientLevel?): Int = when {
            clientLevel == null -> 0
            clientLevel.isThundering -> 2
            clientLevel.isRaining -> 1
            else -> 0
        }

        @Deprecated("")
        @JvmStatic
        fun getBiomeCategory(entity: Entity): String? {
            return null
        }

        @JvmStatic
        fun dumpMods(context: IContext<*>): Any? {
            if (!context.isDebugMode()) {
                return null
            }
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
            if (!context.isDebugMode()) {
                return null
            }
            val activeEffects: Collection<MobEffectInstance> = when (val entity = context.entity()) {
                is Arrow -> {
                    val potionContents: PotionContents =
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
            val biome: Holder<Biome> = context.entity().level().getBiome(context.entity().blockPosition())
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
            val blockPos: BlockPos = entity.blockPosition()
            return entity.level().canSeeSky(blockPos) && entity.level()
                .getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).y <= blockPos.y
        }

        @JvmStatic
        fun getShoulderParrotVariant(player: Player, leftShoulder: Boolean): String {
            if (player !is AbstractClientPlayer) {
                return "empty"
            }
            val variant: Parrot.Variant? = player.getParrotVariantOnShoulder(leftShoulder)
            return variant?.name?.lowercase(Locale.ENGLISH) ?: "empty"
        }

        @JvmStatic
        fun hasShoulderParrot(player: Player, leftShoulder: Boolean): Boolean {
            return player is AbstractClientPlayer && player.getParrotVariantOnShoulder(leftShoulder) != null
        }
    }
}