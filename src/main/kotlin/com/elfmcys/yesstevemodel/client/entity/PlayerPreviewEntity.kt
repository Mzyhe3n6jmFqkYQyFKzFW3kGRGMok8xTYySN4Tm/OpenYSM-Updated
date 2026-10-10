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
import java.util.*

class PlayerPreviewEntity : CustomPlayerEntity(DummyPlayer(), false, false), IPreviewAnimatable {
    override val animationStateMachine: AnimationTracker = AnimationTracker()
    private var customAnimationActive: Boolean = false

    override fun resetModel() {
        animationStateMachine.queuedAnimation = StringPool.EMPTY
        animationStateMachine.currentAnimation = StringPool.EMPTY
        animationStateMachine.previousAnimation = StringPool.EMPTY
        customAnimationActive = false
        super.resetModel()
    }

    override val physicsManager: PhysicsManager
        get() {
            return defaultPhysicsManager
        }

    override fun setCustomAnimationActive(active: Boolean) {
        customAnimationActive = active
    }

    override val isDebugMode: Boolean
        get() = true

    override fun shouldRenderOverlay(): Boolean = customAnimationActive

    override val refreshRate: Int
        get() = ClientTickEvent.refreshRate

    override fun hasCustomTexture(): Boolean = true

    override fun processAnimationImpl(partialTick: Float, z: Boolean): AnimationEvent<*>? {
        if (entity is DummyPlayer && !entity.ensureLevel()) return null
        return super.processAnimationImpl(partialTick, z)
    }

    override fun shouldSkipAnimation(event: AnimationEvent<*>): Boolean = true

    override val logger: ILogger?
        get() = null

    override fun buildRenderShape(modelAssembly: ModelAssembly, isDefault: Boolean): ModelWrapper =
        TexturedModelWrapper(modelAssembly, isDefault, false, true, 300)

    internal class DummyPlayer : AbstractClientPlayer(
        Minecraft.getInstance().level!!,
        createGameProfile()
    ) {
        override fun isSpectator(): Boolean = false

        override fun isCreative(): Boolean = false

        fun ensureLevel(): Boolean {
            val clientLevel: ClientLevel? = Minecraft.getInstance().level
            if (clientLevel != null) {
                setLevel(clientLevel)
                return true
            }
            return false
        }

        companion object {
            fun createGameProfile(): GameProfile {
                val uuid = UUID.randomUUID()
                return GameProfile(uuid, "ysm_" + uuid.toString().replace('-', '_'))
            }
        }
    }

    @Suppress("unused")
    companion object {
        fun isPreviewPlayer(player: Player?): Boolean = player is DummyPlayer
    }
}