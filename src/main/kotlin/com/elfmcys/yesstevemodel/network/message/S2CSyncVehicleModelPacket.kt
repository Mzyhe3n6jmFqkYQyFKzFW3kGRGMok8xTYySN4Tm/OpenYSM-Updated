package com.elfmcys.yesstevemodel.network.message

import com.elfmcys.yesstevemodel.capability.VehicleCapability
import com.elfmcys.yesstevemodel.capability.VehicleModelCapability
import com.elfmcys.yesstevemodel.event.EntityJoinCallbackEvent
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.Entity
import rip.ysm.api.network.PacketContext

class S2CSyncVehicleModelPacket(
    val entityId: Int,
    val capability: VehicleModelCapability,
    val floatMap: Int2FloatOpenHashMap = Int2FloatOpenHashMap(0)
) {
    companion object {
        @JvmStatic
        fun encode(message: S2CSyncVehicleModelPacket, friendlyByteBuf: FriendlyByteBuf) {
            friendlyByteBuf.writeVarInt(message.entityId)
            friendlyByteBuf.writeNbt(message.capability.serializeNBT())
        }

        @JvmStatic
        fun decode(buf: FriendlyByteBuf): S2CSyncVehicleModelPacket {
            val varInt = buf.readVarInt()
            val nbt = buf.readNbt()
            val cap = VehicleModelCapability()
            if (nbt != null) {
                cap.deserializeNBT(nbt)
            }
            val objectMap: Object2FloatOpenHashMap<String> = cap.getMolangVars()
            val floatMap = Int2FloatOpenHashMap()
            objectMap.object2FloatEntrySet().fastForEach { entry ->
                floatMap.put(StringPool.computeIfAbsent(entry.key), entry.floatValue)
            }
            return S2CSyncVehicleModelPacket(varInt, cap, floatMap)
        }

        @JvmStatic
        fun handle(message: S2CSyncVehicleModelPacket, ctx: PacketContext) {
            if (ctx.isClientSide()) {
                EntityJoinCallbackEvent.addCallback(message.entityId) { entity ->
                    handleCapability(entity, message.capability, message.floatMap)
                }
            }
        }

        @JvmStatic
        @Environment(EnvType.CLIENT)
        fun handleCapability(entity: Entity, capability: VehicleModelCapability, floatMap: Int2FloatOpenHashMap) {
            VehicleCapability[entity]?.let { vehicleCapability ->
                vehicleCapability.setOwnerModelId(capability.getOwnerModelId())
                vehicleCapability.setFloatMap(floatMap)
            }
        }
    }
}