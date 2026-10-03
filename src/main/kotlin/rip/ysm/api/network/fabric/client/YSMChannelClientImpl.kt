package rip.ysm.api.network.fabric.client

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.network.protocol.Packet
import net.minecraft.resources.Identifier
import rip.ysm.api.network.fabric.YSMChannelImpl
import rip.ysm.api.network.fabric.YSMPayload

object YSMChannelClientImpl {
    @JvmStatic
    fun init(channelId: Identifier) {
        ClientPlayNetworking.registerGlobalReceiver(YSMPayload.TYPE) { payload, context ->
            YSMChannelImpl.dispatch(
                payload.toBuf(),
                ClientPacketContext(context.client(), context.player().connection.connection)
            )
        }
    }

    @JvmStatic
    fun sendToServer(payload: YSMPayload) {
        ClientPlayNetworking.send(payload)
    }

    @JvmStatic
    fun toServerboundPacket(payload: YSMPayload): Packet<*> {
        return ClientPlayNetworking.createC2SPacket(payload)
    }
}