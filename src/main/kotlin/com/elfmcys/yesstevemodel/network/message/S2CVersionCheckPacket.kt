package com.elfmcys.yesstevemodel.network.message

import com.elfmcys.yesstevemodel.client.ClientModelManager
import com.elfmcys.yesstevemodel.client.ClientOnlyMode
import com.elfmcys.yesstevemodel.network.NetworkHandler
import net.minecraft.network.FriendlyByteBuf
import rip.ysm.api.network.PacketContext

class S2CVersionCheckPacket(val version: String = NetworkHandler.VERSION) {
    companion object {
        fun decode(buf: FriendlyByteBuf): S2CVersionCheckPacket {
            val version = buf.readUtf()
            if (buf.readableBytes() > 0) {
                val brand = buf.readUtf()
                if (brand == "open_ysm:v1") {
                    ClientModelManager.setOysmServer(true)
                    ClientModelManager.setAllowUpload(buf.readBoolean())
                }
            }
            return S2CVersionCheckPacket(version)
        }

        fun encode(message: S2CVersionCheckPacket, buf: FriendlyByteBuf) {
            buf.writeUtf(message.version)
        }

        fun handle(message: S2CVersionCheckPacket, ctx: PacketContext) {
            if (NetworkHandler.setChannelVersion(ctx.connection, message.version)) {
                ctx.enqueueWork {
                    ClientOnlyMode.leaveStandalone()
                    ClientModelManager.onSyncConnected()
                }
            }
            if (NetworkHandler.VERSION == message.version) {
                NetworkHandler.markClientHandshakeComplete()
            }
            ctx.enqueueWork {
                NetworkHandler.sendToServer(C2SVersionCheckPacket())
            }
        }
    }
}