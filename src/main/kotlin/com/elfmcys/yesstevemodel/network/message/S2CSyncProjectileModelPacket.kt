package com.elfmcys.yesstevemodel.network.message

import com.elfmcys.yesstevemodel.capability.ProjectileCapability
import com.elfmcys.yesstevemodel.capability.ProjectileModelCapability
import com.elfmcys.yesstevemodel.event.EntityJoinCallbackEvent
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.Entity
import rip.ysm.api.network.PacketContext

class S2CSyncProjectileModelPacket(
    val entityId: Int,
    val capability: ProjectileModelCapability,
    val floatMap: Int2FloatOpenHashMap = Int2FloatOpenHashMap()
) {
    companion object {
        @JvmStatic
        fun encode(message: S2CSyncProjectileModelPacket, buf: FriendlyByteBuf) {
            buf.writeVarInt(message.entityId)
            buf.writeNbt(message.capability.save())
        }

        @JvmStatic
        fun decode(buf: FriendlyByteBuf): S2CSyncProjectileModelPacket {
            val entityId = buf.readVarInt()
            val nbt = buf.readNbt()
            val cap = if (nbt != null) ProjectileModelCapability.load(nbt) else ProjectileModelCapability()
            val objectMap: Object2FloatOpenHashMap<String> = cap.molangVars
            val floatMap = Int2FloatOpenHashMap()
            objectMap.object2FloatEntrySet().fastForEach { entry ->
                floatMap.put(StringPool.computeIfAbsent(entry.key), entry.floatValue)
            }
            return S2CSyncProjectileModelPacket(entityId, cap, floatMap)
        }

        @JvmStatic
        fun handle(message: S2CSyncProjectileModelPacket, ctx: PacketContext) {
            if (!ctx.isClientSide()) return
            EntityJoinCallbackEvent.addCallback(message.entityId) { entity ->
                handleCapability(entity, message.capability, message.floatMap)
            }
        }

        @JvmStatic
        @Environment(EnvType.CLIENT)
        fun handleCapability(entity: Entity, capability: ProjectileModelCapability, floatMap: Int2FloatOpenHashMap) {
            ProjectileCapability[entity]?.let { projectileCapability ->
                projectileCapability.updateModelId(capability.ownerModelId)
                projectileCapability.setFloatProperties(floatMap)
            }
        }
    }
}