package com.elfmcys.yesstevemodel.network.message

import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.event.EntityJoinCallbackEvent
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.Entity
import rip.ysm.api.network.PacketContext

class S2CSetModelAndTexturePacket(
    val entityId: Int,
    val modelId: String,
    val textureId: String,
    val disabled: Boolean,
    val entityModelSync: S2CSyncPlayerStatePacket
) {
    companion object {
        @JvmStatic
        fun encode(other: S2CSetModelAndTexturePacket, friendlyByteBuf: FriendlyByteBuf) {
            friendlyByteBuf.writeVarInt(other.entityId)
            friendlyByteBuf.writeUtf(other.modelId)
            friendlyByteBuf.writeUtf(other.textureId)
            friendlyByteBuf.writeBoolean(other.disabled)
            S2CSyncPlayerStatePacket.encode(other.entityModelSync, friendlyByteBuf)
        }

        @JvmStatic
        fun decode(friendlyByteBuf: FriendlyByteBuf): S2CSetModelAndTexturePacket {
            return S2CSetModelAndTexturePacket(
                friendlyByteBuf.readVarInt(),
                friendlyByteBuf.readUtf(),
                friendlyByteBuf.readUtf(),
                friendlyByteBuf.readBoolean(),
                S2CSyncPlayerStatePacket.decode(friendlyByteBuf)
            )
        }

        @JvmStatic
        fun handle(other: S2CSetModelAndTexturePacket, ctx: PacketContext) {
            if (ctx.isClientSide()) {
                EntityJoinCallbackEvent.addCallback(other.entityId) { entity ->
                    applyOnClient(entity, other)
                }
            }
        }

        @JvmStatic
        fun applyOnClient(entity: Entity, other: S2CSetModelAndTexturePacket) {
            PlayerCapability[entity]?.let { cap ->
                cap.initModelWithTexture(other.modelId, other.textureId)
                cap.setForceDisabled(other.disabled)
                S2CSyncPlayerStatePacket.handleCapability(entity, other.entityModelSync)
            }
        }
    }
}