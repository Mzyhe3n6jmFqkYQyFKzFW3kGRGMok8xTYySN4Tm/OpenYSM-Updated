package com.elfmcys.yesstevemodel.client.animation.molang

import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.client.animation.Priority
import com.elfmcys.yesstevemodel.client.animation.molang.functions.ctrl.*
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import com.elfmcys.yesstevemodel.extensions.isCrawl
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.EntityFrameStateTracker
import com.elfmcys.yesstevemodel.geckolib3.core.controller.controllers.PlayerAnimationController
import com.elfmcys.yesstevemodel.geckolib3.core.enums.AnimationState
import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ContextBinding
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.IValueEvaluator
import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.player.Player
import rip.ysm.compat.bettercombat.BetterCombatCompat
import rip.ysm.compat.carryon.CarryOnCompat
import rip.ysm.compat.create.CreateCompat
import rip.ysm.compat.gun.tacz.TacCompat
import rip.ysm.compat.immersivemelodies.ImmersiveMelodiesCompat
import rip.ysm.compat.ironsspellbooks.SpellbooksCompat
import rip.ysm.compat.parcool.ParcoolCompat
import rip.ysm.compat.sbackpack.SBackpackCompat
import rip.ysm.compat.slashblade.SlashBladeCompat
import rip.ysm.compat.swem.SWEMCompat
import java.util.function.Predicate
import kotlin.math.abs

object CtrlBinding : ContextBinding() {
    init {
        registerLivingEntityState("death", Priority.HIGHEST, LivingEntity::isDeadOrDying)
        registerLivingEntityState("riptide", Priority.HIGHEST, LivingEntity::isAutoSpinAttack)
        registerLivingEntityState("sleep", Priority.HIGHEST) { it.hasPose(Pose.SLEEPING) }
        registerLivingEntityState("swim", Priority.HIGHEST, Entity::isSwimming)
        registerLivingEntityState("climb", Priority.HIGHEST) {
            it.isCrawl && isWalking(
                it
            )
        }
        registerLivingEntityState("climbing", Priority.HIGHEST) { it.isCrawl }
        registerLivingEntityState(
            "ladder_up",
            Priority.HIGHEST
        ) { it.onClimbable() && getVerticalVelocity(it) > 0.0f }
        registerLivingEntityState(
            "ladder_stillness",
            Priority.HIGHEST
        ) { it.onClimbable() && getVerticalVelocity(it) == 0.0f }
        registerLivingEntityState(
            "ladder_down",
            Priority.HIGHEST
        ) { it.onClimbable() && getVerticalVelocity(it) < 0.0f }
        registerState("fly", Priority.HIGH, CtrlBinding::isFlying)
        registerLivingEntityState(
            "elytra_fly",
            Priority.HIGH
        ) { it.hasPose(Pose.FALL_FLYING) && it.isFallFlying }
        registerLivingEntityState("swim_stand", Priority.NORMAL) { it.isInWater && !it.onGround() }
        registerLivingEntityState("attacked", Priority.NORMAL) { it.hurtTime > 0 }
        registerLivingEntityState("jump", Priority.NORMAL) { !it.onGround() && !it.isInWater }
        registerLivingEntityState(
            "sneak",
            Priority.NORMAL
        ) { it.onGround() && it.hasPose(Pose.CROUCHING) && isWalking(it) }
        registerLivingEntityState(
            "sneaking",
            Priority.NORMAL
        ) { it.onGround() && it.hasPose(Pose.CROUCHING) }
        registerLivingEntityState("run", Priority.LOWEST) { it.onGround() && it.isSprinting }
        registerLivingEntityState("walk", Priority.LOWEST) { it.onGround() && isWalking(it) }
        registerLivingEntityState("idle", Priority.LOWEST) { true }
        `var`("playing_extra_animation", IValueEvaluator { isPlayingExtraAnimation(it) })
        function("hold", HandRenderFunction.createAlways())
        function("swing", HandRenderFunction.createWhenSwinging())
        function("use", HandRenderFunction.createWhenUsing())
        function("armor", Armor.create())
        function("ride", Ride.create())
        CarryOnCompat.registerBindings(this)
        TacCompat.registerControllerFunctions(this)
        SWEMCompat.registerControllerFunctions(this)
        ParcoolCompat.registerBindings(this)
        SlashBladeCompat.registerControllerFunctions(this)
        SBackpackCompat.registerControllerFunctions(this)
        CreateCompat.registerCreateFunctions(this)
        BetterCombatCompat.registerBindings(this)
        ImmersiveMelodiesCompat.registerBindings(this)
        SpellbooksCompat.registerBindings(this)
        constValue("state_continue", 2)
        constValue("state_stop", 3)
        constValue("state_pause", 4)
        constValue("state_bypass", 5)
        constValue("loop", 10)
        constValue("play_once", 11)
        constValue("hold_on_last_frame", 12)
        function("set_animation", SetAnimation())
        function("set_beginning_transition_length", SetTransitionSpeed())
        function("reset", Reset())
        function("indicate_reload", IndicateReload())
    }

    private fun registerState(name: String, priority: Int, predicate: Predicate<IContext<LivingEntity>>) {
        if (data == null) data = Array(Priority.LOWEST + 1) { ReferenceArrayList(6) }
        (data ?: return)[priority].add(AnimationStatePredicate(name, priority, predicate))
        livingEntityVar(name) { ctx: IContext<LivingEntity> -> evaluateState(name, ctx) }
    }

    private fun registerLivingEntityState(name: String, priority: Int, predicate: EntityCondition) {
        registerState(name, priority, predicate)
    }

    private data class AnimationStatePredicate(
        val name: String,
        val priority: Int,
        val predicate: Predicate<IContext<LivingEntity>>
    )

    private fun interface EntityCondition : Predicate<IContext<LivingEntity>> {
        fun check(entity: LivingEntity): Boolean
        override fun test(context: IContext<LivingEntity>): Boolean {
            return check(context.entity())
        }
    }

    private var data: Array<ReferenceArrayList<AnimationStatePredicate>>? = null

    @JvmStatic
    fun isPlayingExtraAnimation(context: IContext<Any>): Boolean {
        val animatableEntity = context.geoInstance()
        return animatableEntity is CustomPlayerEntity && animatableEntity.isModelSwitching && animatableEntity.getAnimationState(
            PlayerAnimationController.CAP_CONTROLLER_KEY
        ) != AnimationState.IDLE
    }

    @JvmStatic
    fun evaluateState(name: String, context: IContext<LivingEntity>): Boolean {
        val livingEntity: LivingEntity = context.entity()
        val positionTracker: EntityFrameStateTracker<*> = context.geoInstance().positionTracker
        if (positionTracker.getCachedModelId() != null) {
            return name == positionTracker.getCachedModelId()
        }
        if (context.geoInstance() is IPreviewAnimatable) {
            positionTracker.setCachedModelId(StringPool.EMPTY)
            return false
        }
        if (livingEntity is Player && ParcoolCompat.isPlayerParcooling(livingEntity)) {
            positionTracker.setCachedModelId(StringPool.EMPTY)
            return false
        }
        val vehicle: Entity? = livingEntity.vehicle
        if (vehicle != null && vehicle.isAlive) {
            positionTracker.setCachedModelId(StringPool.EMPTY)
            return false
        }
        val stateData = data ?: return false
        for (i in 0..4) {
            for ((name1, _, predicate) in stateData[i]) {
                if (predicate.test(context)) {
                    positionTracker.setCachedModelId(name1)
                    return name1 == name
                }
            }
        }
        positionTracker.setCachedModelId(StringPool.EMPTY)
        return false
    }

    @JvmStatic
    fun isWalking(livingEntity: LivingEntity): Boolean = abs(
        livingEntity.walkAnimation.speed(
            Minecraft.getInstance().deltaTracker.getGameTimeDeltaPartialTick(
                false
            )
        )
    ) > 0.05f

    @JvmStatic
    fun getVerticalVelocity(livingEntity: LivingEntity): Float =
        20.0f * (livingEntity.position().y - livingEntity.yo).toFloat()

    @JvmStatic
    fun isFlying(context: IContext<LivingEntity>): Boolean {
        val animatableEntity: AnimatableEntity<*> = context.geoInstance()
        if (animatableEntity is PlayerCapability) {
            if (!animatableEntity.isLocalPlayerModel) {
                return animatableEntity.positionTracker.isFlying()
            }
        }
        val entity: Entity = context.entity()
        return entity is Player && entity.abilities.flying
    }
}