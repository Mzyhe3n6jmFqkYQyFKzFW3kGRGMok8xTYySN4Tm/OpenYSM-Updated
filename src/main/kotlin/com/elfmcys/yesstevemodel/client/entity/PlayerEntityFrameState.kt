@file:Suppress("MemberVisibilityCanBePrivate")

package com.elfmcys.yesstevemodel.client.entity

import com.elfmcys.yesstevemodel.network.message.S2CSyncPlayerStatePacket
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap
import net.minecraft.core.Holder
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.player.Player

open class PlayerEntityFrameState(player: Player, val isLocalPlayer: Boolean) : LivingEntityFrameState<Player>(player) {
    val effectAmplifiers: Object2ByteOpenHashMap<Holder<MobEffect>> = Object2ByteOpenHashMap(8)
    private var flying: Boolean = false
    var experienceLevel: Int = 0
        private set
    var health: Int = 0
        private set
    var maxHealth: Int = 0
        private set
    var foodLevel: Int = 0
        private set
    var strafeInput: Float = 0.0f
        private set
    var verticalInput: Float = 0.0f
        private set
    var forwardInput: Float = 0.0f
        private set
    var isShieldBlocking: Boolean = false
        private set

    override fun reset() {
        super.reset()
        effectAmplifiers.clear()
        flying = false
        experienceLevel = 0
        health = 0
        maxHealth = 0
        foodLevel = 0
        strafeInput = 0.0f
        verticalInput = 0.0f
        forwardInput = 0.0f
        isShieldBlocking = false
    }

    open fun applySyncMessage(message: S2CSyncPlayerStatePacket) {
        val flags = message.flags.toInt()
        if (flags and 2 != 0) flying = message.isFlying
        if (flags and 4 != 0) {
            if (message.isFullSync()) effectAmplifiers.clear()
            message.effectAmplifiers?.let {
                effectAmplifiers.putAll(it)
            }
        }
        if (flags and 8 != 0) experienceLevel = message.experienceLevel
        if (flags and 16 != 0) foodLevel = message.foodLevel
        if (flags and 32 != 0) health = message.health
        if (flags and 64 != 0) maxHealth = message.maxHealth
        if (flags and 128 != 0) strafeInput = message.strafeInput / 127.0f
        if (flags and 256 != 0) verticalInput = message.verticalInput / 127.0f
        if (flags and 512 != 0) forwardInput = message.forwardInput / 127.0f
        if (flags and 1024 != 0) isShieldBlocking = message.shieldBlockCooldown
    }

    open val isFlying: Boolean
        get() {
            if (isLocalPlayer) return entity.abilities.flying
            return flying
        }

    open fun getEffectAmplifier(mobEffect: Holder<MobEffect>): Byte {
        if (isLocalPlayer) {
            val effect: MobEffectInstance? = entity.getEffect(mobEffect)
            if (effect != null) {
                return (effect.amplifier + 1).toByte()
            }
            return 0
        }
        return effectAmplifiers.getOrDefault(mobEffect, 0)
    }

    override fun onTickUpdate(currentTick: Int, previousTick: Int) {
        if (isLocalPlayer) updateHeadYaw(entity, currentTick, previousTick)
        super.onTickUpdate(currentTick, previousTick)
    }

    companion object {
        var headYawDelta: Float = 0.0f
            private set

        private var lastYRot: Float = 0.0f

        fun updateHeadYaw(player: Player, currentTick: Int, previousTick: Int) {
            val yRot = player.yRot
            if (previousTick > 0) headYawDelta = (yRot - lastYRot) * 20.0f / (currentTick - previousTick)
            lastYRot = yRot
        }
    }
}