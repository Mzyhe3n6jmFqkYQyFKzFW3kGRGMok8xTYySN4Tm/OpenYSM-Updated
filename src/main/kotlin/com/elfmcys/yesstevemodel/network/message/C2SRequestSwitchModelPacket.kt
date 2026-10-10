package com.elfmcys.yesstevemodel.network.message

import com.elfmcys.yesstevemodel.capability.ModelInfoCapability
import com.elfmcys.yesstevemodel.config.ServerConfig
import com.elfmcys.yesstevemodel.model.ServerModelManager
import com.elfmcys.yesstevemodel.model.ServerModelSelection
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerPlayer
import rip.ysm.api.network.PacketContext

class C2SRequestSwitchModelPacket(
    val modelId: String,
    val textureId: String
) {
    companion object {
        @JvmStatic
        fun encode(message: C2SRequestSwitchModelPacket, buf: FriendlyByteBuf) {
            buf.writeUtf(message.modelId)
            buf.writeUtf(message.textureId)
        }

        @JvmStatic
        fun decode(buf: FriendlyByteBuf): C2SRequestSwitchModelPacket {
            return C2SRequestSwitchModelPacket(buf.readUtf(), buf.readUtf())
        }

        @JvmStatic
        fun handle(message: C2SRequestSwitchModelPacket, ctx: PacketContext) {
            if (ctx.isServerSide()) {
                val sender = ctx.sender
                ctx.enqueueWork {
                    if (sender != null && ServerConfig.CAN_SWITCH_MODEL.get()) {
                        handleCapability(message, sender)
                    }
                }
            }
        }

        @JvmStatic
        fun handleCapability(message: C2SRequestSwitchModelPacket, sender: ServerPlayer) {
            ModelInfoCapability[sender]?.let { cap ->
                val str = message.modelId
                val serverModelInfo = ServerModelManager.serverModelInfo
                val serverModelData = serverModelInfo[str]
                val hasAuth =
                    !ServerModelManager.authModels.contains(str) || ServerModelSelection.hasAuthModel(sender.uuid, str)
                if (serverModelData == null ||
                    !hasAuth ||
                    !serverModelData.modelInfo.textures.contains(message.textureId)
                ) {
                    cap.resetToDefault()
                    ServerModelSelection.savePlayerSelection(sender.uuid, cap.modelId, cap.selectTexture)
                } else {
                    cap.setModelAndTexture(message.modelId, message.textureId)
                    ServerModelSelection.savePlayerSelection(sender.uuid, message.modelId, message.textureId)
                }
                cap.stopAnimation(sender)
            }
        }
    }
}