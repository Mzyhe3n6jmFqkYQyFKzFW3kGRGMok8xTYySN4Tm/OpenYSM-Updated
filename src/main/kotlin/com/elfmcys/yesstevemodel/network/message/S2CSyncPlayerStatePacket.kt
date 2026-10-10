package com.elfmcys.yesstevemodel.network.message

import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.event.EntityJoinCallbackEvent
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import it.unimi.dsi.fastutil.ints.Int2FloatArrayMap
import it.unimi.dsi.fastutil.ints.Int2FloatMap
import it.unimi.dsi.fastutil.ints.Int2FloatMaps
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap
import it.unimi.dsi.fastutil.objects.*
import net.minecraft.core.Holder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import org.joml.Math
import rip.ysm.api.network.PacketContext

class S2CSyncPlayerStatePacket(
    @JvmField var entityId: Int
) {
    var flags: Short = 0

    var isFlying: Boolean = false

    var effectAmplifiers: Object2ByteMap<Holder<MobEffect>>? = null

    var experienceLevel: Int = 0

    var foodLevel: Int = 0

    var health: Int = 0

    var maxHealth: Int = 0

    var strafeInput: Byte = 0

    var verticalInput: Byte = 0

    var forwardInput: Byte = 0

    var shieldBlockCooldown: Boolean = false

    var modelSwitchId: String? = null
    var molangHashId: Int = 0

    var molangVars: Object2FloatMap<String>? = null

    var molangVarData: Int2FloatMap? = null

    fun isEmpty(): Boolean = flags.toInt() == 0

    fun isFullSync(): Boolean = (flags.toInt() and 1) != 0

    fun markFullSync() {
        flags = (flags.toInt() or 1).toShort()
    }

    fun reset(entityId: Int) {
        this.entityId = entityId
        flags = 0
        effectAmplifiers = null
        molangVars = null
    }

    fun setFlying(isFlying: Boolean): S2CSyncPlayerStatePacket {
        flags = (flags.toInt() or 2).toShort()
        this.isFlying = isFlying
        return this
    }

    fun addEffect(effect: Holder<MobEffect>, amplifier: Int): S2CSyncPlayerStatePacket {
        flags = (flags.toInt() or 4).toShort()
        val current = effectAmplifiers
        if (current == null) {
            effectAmplifiers = Object2ByteMaps.singleton(effect, amplifier.toByte())
        } else {
            if (current.size == 1) {
                val map = Object2ByteOpenHashMap(current)
                map.put(effect, amplifier.toByte())
                effectAmplifiers = map
            } else {
                current.put(effect, amplifier.toByte())
            }
        }
        return this
    }

    fun setEffects(effects: Object2ByteMap<Holder<MobEffect>>): S2CSyncPlayerStatePacket {
        flags = (flags.toInt() or 4).toShort()
        effectAmplifiers = effects
        return this
    }

    fun removeEffect(effect: Holder<MobEffect>): S2CSyncPlayerStatePacket {
        addEffect(effect, 0)
        return this
    }

    fun setExperienceLevel(level: Int): S2CSyncPlayerStatePacket {
        flags = (flags.toInt() or 8).toShort()
        experienceLevel = level
        return this
    }

    fun setFoodLevel(level: Int): S2CSyncPlayerStatePacket {
        flags = (flags.toInt() or 16).toShort()
        foodLevel = level
        return this
    }

    fun setHealth(health: Int): S2CSyncPlayerStatePacket {
        flags = (flags.toInt() or 32).toShort()
        this.health = health
        return this
    }

    fun setMaxHealth(maxHealth: Int): S2CSyncPlayerStatePacket {
        flags = (flags.toInt() or 64).toShort()
        this.maxHealth = maxHealth
        return this
    }

    fun setStrafeInput(input: Float): S2CSyncPlayerStatePacket {
        flags = (flags.toInt() or 128).toShort()
        strafeInput = Math.round(Math.clamp(input, -1.0f, 1.0f) * 127.0f).toByte()
        return this
    }

    fun setVerticalInput(input: Float): S2CSyncPlayerStatePacket {
        flags = (flags.toInt() or 256).toShort()
        verticalInput = Math.round(Math.clamp(input, -1.0f, 1.0f) * 127.0f).toByte()
        return this
    }

    fun setForwardInput(input: Float): S2CSyncPlayerStatePacket {
        flags = (flags.toInt() or 512).toShort()
        forwardInput = Math.round(Math.clamp(input, -1.0f, 1.0f) * 127.0f).toByte()
        return this
    }

    fun setShieldBlockCooldown(onCooldown: Boolean): S2CSyncPlayerStatePacket {
        flags = (flags.toInt() or 1024).toShort()
        shieldBlockCooldown = onCooldown
        return this
    }

    fun setModelSwitch(modelId: String): S2CSyncPlayerStatePacket {
        flags = (flags.toInt() or 2048).toShort()
        modelSwitchId = modelId
        return this
    }

    fun setMolangVars(hashId: Int, variables: Object2FloatMap<String>): S2CSyncPlayerStatePacket {
        val currentVars = molangVars
        if (currentVars == null || molangHashId != hashId) {
            flags = (flags.toInt() or 4096).toShort()
            molangHashId = hashId
            molangVars = Object2FloatOpenHashMap(variables)
        } else {
            currentVars.putAll(variables)
        }
        return this
    }

    companion object {
        fun encode(message: S2CSyncPlayerStatePacket, buffer: FriendlyByteBuf) {
            buffer.writeVarInt(message.entityId)
            buffer.writeShort(message.flags.toInt())
            val flags = message.flags.toInt()
            if (flags and 2 != 0) buffer.writeBoolean(message.isFlying)
            if (flags and 4 != 0) {
                val effects = message.effectAmplifiers ?: Object2ByteMaps.emptyMap()
                buffer.writeVarInt(effects.size)
                Object2ByteMaps.fastForEach(effects) { entry ->
                    buffer.writeVarInt(BuiltInRegistries.MOB_EFFECT.asHolderIdMap().getId(entry.key))
                    buffer.writeByte(entry.byteValue.toInt())
                }
            }
            if (flags and 8 != 0) buffer.writeVarInt(message.experienceLevel)
            if (flags and 16 != 0) buffer.writeVarInt(message.foodLevel)
            if (flags and 32 != 0) buffer.writeVarInt(message.health)
            if (flags and 64 != 0) buffer.writeVarInt(message.maxHealth)
            if (flags and 128 != 0) buffer.writeByte(message.strafeInput.toInt())
            if (flags and 256 != 0) buffer.writeByte(message.verticalInput.toInt())
            if (flags and 512 != 0) buffer.writeByte(message.forwardInput.toInt())
            if (flags and 1024 != 0) buffer.writeBoolean(message.shieldBlockCooldown)
            if (flags and 2048 != 0) buffer.writeUtf(message.modelSwitchId ?: "")
            if (flags and 4096 != 0) {
                buffer.writeInt(message.molangHashId)
                val vars = message.molangVars ?: Object2FloatMaps.emptyMap()
                buffer.writeVarInt(vars.size)
                Object2FloatMaps.fastForEach(vars) { entry ->
                    buffer.writeUtf(entry.key)
                    buffer.writeFloat(entry.floatValue)
                }
            }
        }

        fun decode(buffer: FriendlyByteBuf): S2CSyncPlayerStatePacket {
            val entityId = buffer.readVarInt()
            val flags = buffer.readShort()
            val message = S2CSyncPlayerStatePacket(entityId)
            message.flags = flags
            val flagsInt = flags.toInt()
            if (flagsInt and 2 != 0) message.isFlying = buffer.readBoolean()
            if (flagsInt and 4 != 0) {
                when (val effectCount = buffer.readVarInt()) {
                    0 -> {
                        message.effectAmplifiers = Object2ByteMaps.emptyMap()
                    }

                    1 -> {
                        val holder = BuiltInRegistries.MOB_EFFECT.asHolderIdMap().byId(buffer.readVarInt())
                        if (holder != null) {
                            message.effectAmplifiers = Object2ByteMaps.singleton(holder, buffer.readByte())
                        }
                    }

                    else -> {
                        val map = Object2ByteArrayMap<Holder<MobEffect>>(effectCount)
                        for (i in 0 until effectCount) {
                            val holder = BuiltInRegistries.MOB_EFFECT.asHolderIdMap().byId(buffer.readVarInt())
                            val amplifier = buffer.readByte()
                            if (holder != null) {
                                map.put(holder, amplifier)
                            }
                        }
                        message.effectAmplifiers = map
                    }
                }
            }
            if (flagsInt and 8 != 0) message.experienceLevel = buffer.readVarInt()
            if (flagsInt and 16 != 0) message.foodLevel = buffer.readVarInt()
            if (flagsInt and 32 != 0) message.health = buffer.readVarInt()
            if (flagsInt and 64 != 0) message.maxHealth = buffer.readVarInt()
            if (flagsInt and 128 != 0) message.strafeInput = buffer.readByte()
            if (flagsInt and 256 != 0) message.verticalInput = buffer.readByte()
            if (flagsInt and 512 != 0) message.forwardInput = buffer.readByte()
            if (flagsInt and 1024 != 0) message.shieldBlockCooldown = buffer.readBoolean()
            if (flagsInt and 2048 != 0) message.modelSwitchId = buffer.readUtf()
            if (flagsInt and 4096 != 0) {
                message.molangHashId = buffer.readInt()
                val varCount = buffer.readVarInt()
                when {
                    message.isFullSync() -> {
                        val roamingVars = Int2FloatOpenHashMap(varCount)
                        message.molangVarData = roamingVars
                        repeat(varCount) {
                            roamingVars.put(StringPool.computeIfAbsent(buffer.readUtf()), buffer.readFloat())
                        }
                    }

                    else -> {
                        when (varCount) {
                            0 -> {
                                message.molangVarData = Int2FloatMaps.EMPTY_MAP
                            }

                            1 -> {
                                message.molangVarData =
                                    Int2FloatMaps.singleton(
                                        StringPool.computeIfAbsent(buffer.readUtf()),
                                        buffer.readFloat()
                                    )
                            }

                            else -> {
                                val keys = IntArray(varCount)
                                val values = FloatArray(varCount)
                                for (i in 0 until varCount) {
                                    keys[i] = StringPool.computeIfAbsent(buffer.readUtf())
                                    values[i] = buffer.readFloat()
                                }
                                message.molangVarData = Int2FloatArrayMap(keys, values)
                            }
                        }
                    }
                }
            }
            return message
        }

        fun handle(message: S2CSyncPlayerStatePacket, ctx: PacketContext) {
            if (ctx.isClientSide()) {
                EntityJoinCallbackEvent.addCallback(message.entityId) { entity ->
                    handleCapability(entity, message)
                }
            }
        }

        fun handleCapability(entity: Entity, message: S2CSyncPlayerStatePacket) {
            if (entity is Player) {
                PlayerCapability[entity]?.let { cap ->
                    val flags = message.flags.toInt()
                    if (flags and 2048 != 0) {
                        val switchId = message.modelSwitchId
                        when {
                            !switchId.isNullOrEmpty() -> cap.requestModelSwitch(switchId)
                            else -> cap.clearModelSwitch()
                        }
                    }
                    if (flags and 4096 != 0) {
                        val varData = message.molangVarData
                        when {
                            message.isFullSync() -> {
                                if (varData is Int2FloatOpenHashMap) {
                                    cap.updateMolangVars(message.molangHashId, varData)
                                }
                            }

                            varData != null -> {
                                cap.enqueueMolangDelta(message.molangHashId, varData)
                            }
                        }
                    }
                    cap.positionTracker.applySyncMessage(message)
                }
            }
        }
    }
}