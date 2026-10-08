package com.elfmcys.yesstevemodel.network.sync

import com.elfmcys.yesstevemodel.client.event.ShieldBlockCooldownEvent
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.network.NetworkHandler
import com.elfmcys.yesstevemodel.network.message.S2CSyncPlayerStatePacket
import com.elfmcys.yesstevemodel.util.TickCounter
import it.unimi.dsi.fastutil.objects.Object2ByteArrayMap
import it.unimi.dsi.fastutil.objects.Object2ByteMaps
import it.unimi.dsi.fastutil.objects.Object2FloatMap
import net.minecraft.core.Holder
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffect
import org.apache.commons.lang3.StringUtils

class PlayerStateSynchronizer {
    private var tickCounter: TickCounter? = null
    private var dirty: Boolean = false
    private var isFlying: Boolean = false
    private var experienceLevel: Int = -1
    private var health: Int = -1
    private var maxHealth: Int = -1
    private var foodLevel: Int = -1
    private var strafeInput: Float = 0.0f
    private var verticalInput: Float = 0.0f
    private var forwardInput: Float = 0.0f
    private var isShieldBlocking: Boolean = false
    private var syncedModelId: String = StringPool.EMPTY
    private var syncMessage: S2CSyncPlayerStatePacket = S2CSyncPlayerStatePacket(-1)

    init {
        setDirty(false)
    }

    private fun setDirty(isDirty: Boolean) {
        if (isDirty == dirty && tickCounter != null) return
        dirty = isDirty
        tickCounter = if (dirty) TickCounter(3, 3.0f) else TickCounter(4, 7.0f)
    }

    private fun getOrCreateSyncMessage(serverPlayer: ServerPlayer, sendNow: Boolean): S2CSyncPlayerStatePacket {
        if (!sendNow || syncMessage.entityId != serverPlayer.id) syncMessage.reset(serverPlayer.id)
        return syncMessage
    }

    private fun trySendSync(serverPlayer: ServerPlayer) {
        if (!syncMessage.isEmpty() && tickCounter?.tryIncrement() == true) {
            NetworkHandler.sendToTrackingEntityAndSelf(syncMessage, serverPlayer)
            syncMessage = S2CSyncPlayerStatePacket(serverPlayer.id)
        }
    }

    fun updateAndSync(serverPlayer: ServerPlayer, sendNow: Boolean, isDirty: Boolean) {
        setDirty(isDirty)
        val message = getOrCreateSyncMessage(serverPlayer, sendNow)
        if (experienceLevel != serverPlayer.experienceLevel) {
            experienceLevel = serverPlayer.experienceLevel
            if (sendNow) {
                message.setExperienceLevel(experienceLevel)
            }
        }
        if (isFlying != serverPlayer.abilities.flying) {
            isFlying = serverPlayer.abilities.flying
            if (sendNow) {
                message.setFlying(isFlying)
            }
        }
        val currentHealth = serverPlayer.health.toInt()
        if (health != currentHealth) {
            health = currentHealth
            if (sendNow) {
                message.setHealth(health)
            }
        }
        val currentMaxHealth = serverPlayer.maxHealth.toInt()
        if (maxHealth != currentMaxHealth) {
            maxHealth = currentMaxHealth
            if (sendNow) {
                message.setMaxHealth(maxHealth)
            }
        }
        val currentFoodLevel = serverPlayer.foodData.foodLevel
        if (foodLevel != currentFoodLevel) {
            foodLevel = currentFoodLevel
            if (sendNow) {
                message.setFoodLevel(foodLevel)
            }
        }
        if (strafeInput != serverPlayer.xxa) {
            strafeInput = serverPlayer.xxa
            if (sendNow) {
                message.setStrafeInput(strafeInput)
            }
        }
        if (verticalInput != serverPlayer.yya) {
            verticalInput = serverPlayer.yya
            if (sendNow) {
                message.setVerticalInput(verticalInput)
            }
        }
        if (forwardInput != serverPlayer.zza) {
            forwardInput = serverPlayer.zza
            if (sendNow) {
                message.setForwardInput(forwardInput)
            }
        }
        val onCooldown = ShieldBlockCooldownEvent.isOnCooldown(serverPlayer)
        if (isShieldBlocking != onCooldown) {
            isShieldBlocking = onCooldown
            if (sendNow) {
                message.setShieldBlockCooldown(isShieldBlocking)
            }
        }
        if (sendNow) {
            trySendSync(serverPlayer)
        }
    }

    fun syncEffectAdded(serverPlayer: ServerPlayer, effect: Holder<MobEffect>, amplifier: Int) {
        getOrCreateSyncMessage(serverPlayer, true).addEffect(effect, amplifier)
    }

    fun syncEffectRemoved(serverPlayer: ServerPlayer, effect: Holder<MobEffect>) {
        getOrCreateSyncMessage(serverPlayer, true).removeEffect(effect)
    }

    fun syncModelSwitch(serverPlayer: ServerPlayer, sendNow: Boolean, modelId: String) {
        if (!StringUtils.isEmpty(modelId) || !StringUtils.isEmpty(syncedModelId)) {
            syncedModelId = modelId
            getOrCreateSyncMessage(serverPlayer, sendNow).setModelSwitch(modelId)
            if (sendNow) {
                trySendSync(serverPlayer)
            }
        }
    }

    fun syncMolangVars(serverPlayer: ServerPlayer, sendNow: Boolean, hashId: Int, variables: Object2FloatMap<String>) {
        if (!dirty && sendNow) {
            getOrCreateSyncMessage(serverPlayer, true).setMolangVars(hashId, variables)
            trySendSync(serverPlayer)
        }
    }

    fun buildFullSyncMessage(serverPlayer: ServerPlayer, resetMessage: Boolean): S2CSyncPlayerStatePacket {
        if (resetMessage) {
            syncMessage.reset(serverPlayer.id)
        }
        val message = S2CSyncPlayerStatePacket(serverPlayer.id)
        message.markFullSync()
        message.setFlying(serverPlayer.abilities.flying)
        message.setExperienceLevel(serverPlayer.experienceLevel)
        message.setFoodLevel(serverPlayer.foodData.foodLevel)
        val activeEffects = serverPlayer.activeEffects
        when {
            activeEffects.isEmpty() -> {
                message.setEffects(Object2ByteMaps.emptyMap())
            }

            activeEffects.size == 1 -> {
                val instance = activeEffects.iterator().next()
                message.setEffects(Object2ByteMaps.singleton(instance.effect, (instance.amplifier + 1).toByte()))
            }

            else -> {
                val map = Object2ByteArrayMap<Holder<MobEffect>>(activeEffects.size)
                for (instance in activeEffects) {
                    map.put(instance.effect, (instance.amplifier + 1).toByte())
                }
                message.setEffects(map)
            }
        }
        message.setHealth(serverPlayer.health.toInt())
        message.setMaxHealth(serverPlayer.maxHealth.toInt())
        message.setStrafeInput(serverPlayer.xxa)
        message.setVerticalInput(serverPlayer.yya)
        message.setForwardInput(serverPlayer.zza)
        if (ShieldBlockCooldownEvent.isOnCooldown(serverPlayer)) {
            message.setShieldBlockCooldown(true)
        }
        message.setModelSwitch(syncedModelId)
        return message
    }
}