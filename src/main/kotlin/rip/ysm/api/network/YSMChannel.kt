package rip.ysm.api.network

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import rip.ysm.api.network.fabric.YSMChannelImpl

object YSMChannel {
    fun init(channelId: Identifier, version: String) {
        YSMChannelImpl.init(channelId, version)
    }

    fun <T : Any> register(
        discriminator: Int,
        type: Class<T>,
        encoder: (T, FriendlyByteBuf) -> Unit,
        decoder: (FriendlyByteBuf) -> T,
        handler: (T, PacketContext) -> Unit,
        direction: PacketDirection
    ) = YSMChannelImpl.register(discriminator, type, encoder, decoder, handler, direction)

    fun sendToServer(packet: Any) {
        YSMChannelImpl.sendToServer(packet)
    }

    fun sendToClientPlayer(packet: Any, player: ServerPlayer) {
        YSMChannelImpl.sendToClientPlayer(packet, player)
    }

    fun sendToAll(packet: Any) {
        YSMChannelImpl.sendToAll(packet)
    }

    fun sendToTrackingEntity(packet: Any, entity: Entity) {
        YSMChannelImpl.sendToTrackingEntity(packet, entity)
    }

    fun sendToTrackingEntityAndSelf(packet: Any, player: Player) {
        YSMChannelImpl.sendToTrackingEntityAndSelf(packet, player)
    }

    fun toClientboundPacket(packet: Any): Packet<*> {
        return YSMChannelImpl.toClientboundPacket(packet)
    }

    fun toServerboundPacket(packet: Any): Packet<*> {
        return YSMChannelImpl.toServerboundPacket(packet)
    }
}