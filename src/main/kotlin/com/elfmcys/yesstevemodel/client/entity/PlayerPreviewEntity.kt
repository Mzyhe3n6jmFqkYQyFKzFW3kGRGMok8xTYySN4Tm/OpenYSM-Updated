package com.elfmcys.yesstevemodel.client.entity

import com.elfmcys.yesstevemodel.client.animation.AnimationTracker
import com.elfmcys.yesstevemodel.client.animation.molang.PhysicsManager
import com.elfmcys.yesstevemodel.client.event.ClientTickEvent
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.util.log.ILogger
import com.mojang.authlib.GameProfile
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.world.entity.player.Player
import java.util.UUID

class PlayerPreviewEntity : CustomPlayerEntity(DummyPlayer(), false, false), IPreviewAnimatable {
    private val animationStateMachine: AnimationTracker = AnimationTracker()
    private var customAnimationActive: Boolean = false

    override fun resetModel() {
        animationStateMachine.queuedAnimation = StringPool.EMPTY
        animationStateMachine.currentAnimation = StringPool.EMPTY
        animationStateMachine.previousAnimation = StringPool.EMPTY
        customAnimationActive = false
        super.resetModel()
    }

    override fun getAnimationStateMachine(): AnimationTracker {
        return animationStateMachine
    }

    override fun getPhysicsManager(): PhysicsManager {
        return defaultPhysicsManager
    }

    override fun setCustomAnimationActive(active: Boolean) {
        customAnimationActive = active
    }

    override fun isDebugMode(): Boolean {
        return true
    }

    override fun shouldRenderOverlay(): Boolean {
        return customAnimationActive
    }

    override fun getRefreshRate(): Int {
        return ClientTickEvent.getRefreshRate()
    }

    override fun hasCustomTexture(): Boolean {
        return true
    }

    override fun processAnimationImpl(partialTick: Float, isFirstPerson: Boolean): AnimationEvent<*>? {
        val dummy = entity
        if (dummy is DummyPlayer && !dummy.ensureLevel()) {
            return null
        }
        return super.processAnimationImpl(partialTick, isFirstPerson)
    }

    override fun shouldSkipAnimation(event: AnimationEvent<*>): Boolean {
        return true
    }

    override fun getLogger(): ILogger? {
        return null
    }

    override fun buildRenderShape(modelAssembly: ModelAssembly, isDefault: Boolean): ModelWrapper? {
        return TexturedModelWrapper(modelAssembly, isDefault, false, true, 300)
    }

    private class DummyPlayer : AbstractClientPlayer(
        Minecraft.getInstance().level!!,
        createGameProfile()
    ) {
        override fun isSpectator(): Boolean {
            return false
        }

        override fun isCreative(): Boolean {
            return false
        }

        fun ensureLevel(): Boolean {
            val clientLevel: ClientLevel? = Minecraft.getInstance().level
            if (clientLevel != null) {
                setLevel(clientLevel)
                return true
            }
            return false
        }

        companion object {
            @JvmStatic
            fun createGameProfile(): GameProfile {
                val uuid = UUID.randomUUID()
                return GameProfile(uuid, "ysm_" + uuid.toString().replace('-', '_'))
            }
        }
    }

    companion object {
        @JvmStatic
        fun isPreviewPlayer(player: Player): Boolean {
            return player is DummyPlayer
        }
    }
}