package rip.ysm.api.network.fabric

import com.elfmcys.yesstevemodel.access.ServerCommonPacketListenerImplAccessor
import io.netty.buffer.Unpooled
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.resources.Identifier
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import rip.ysm.api.network.PacketContext
import rip.ysm.api.network.PacketDirection
import rip.ysm.api.network.fabric.client.YSMChannelClientImpl

object YSMChannelImpl {
    private val CODECS_BY_ID: MutableMap<Int, Codec<*>> = HashMap()
    private val ID_BY_CLASS: MutableMap<Class<*>, Int> = HashMap()
    private var channelId: Identifier? = null

    @Volatile
    private var currentServer: MinecraftServer? = null

    @JvmStatic
    fun init(id: Identifier, version: String) {
        channelId = id
        YSMPayload.init(id)
        PayloadTypeRegistry.playC2S().register(YSMPayload.TYPE, YSMPayload.CODEC)
        PayloadTypeRegistry.playS2C().register(YSMPayload.TYPE, YSMPayload.CODEC)
        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            currentServer = server
        }
        ServerLifecycleEvents.SERVER_STOPPING.register {
            currentServer = null
        }
        ServerPlayNetworking.registerGlobalReceiver(YSMPayload.TYPE) { payload, context ->
            val conn =
                ((context.player().connection as Any) as ServerCommonPacketListenerImplAccessor).`ysm$getConnection`()
            dispatch(payload.toBuf(), ServerPacketContext(context.server(), context.player(), conn))
        }
        if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
            YSMChannelClientImpl.init(id)
        }
    }

    @JvmStatic
    fun <T : Any> register(
        discriminator: Int,
        type: Class<T>,
        encoder: (T, FriendlyByteBuf) -> Unit,
        decoder: (FriendlyByteBuf) -> T,
        handler: (T, PacketContext) -> Unit,
        direction: PacketDirection
    ) {
        if ((discriminator and 0xff.inv()) != 0) {
            throw IllegalArgumentException("Discriminator must fit in an unsigned byte (0-255): $discriminator")
        }
        val codec = Codec(type, encoder, decoder, handler)
        val maskedId = discriminator and 0xff
        CODECS_BY_ID[maskedId] = codec
        ID_BY_CLASS[type] = maskedId
    }

    @JvmStatic
    fun dispatch(buf: FriendlyByteBuf, ctx: PacketContext) {
        val discriminator = buf.readUnsignedByte().toInt()
        val codec = CODECS_BY_ID[discriminator]
        codec?.dispatch(buf, ctx)
    }

    @JvmStatic
    fun sendToServer(packet: Any) {
        if (FabricLoader.getInstance().environmentType != EnvType.CLIENT) {
            return
        }
        YSMChannelClientImpl.sendToServer(encodePayload(packet))
    }

    @JvmStatic
    fun sendToClientPlayer(packet: Any, player: ServerPlayer) {
        ServerPlayNetworking.send(player, encodePayload(packet))
    }

    @JvmStatic
    fun sendToAll(packet: Any) {
        val server = currentServer ?: return
        val payload = encodePayload(packet)
        for (player in PlayerLookup.all(server)) {
            ServerPlayNetworking.send(player, payload)
        }
    }

    @JvmStatic
    fun sendToTrackingEntity(packet: Any, entity: Entity) {
        val payload = encodePayload(packet)
        for (player in PlayerLookup.tracking(entity)) {
            ServerPlayNetworking.send(player, payload)
        }
    }

    @JvmStatic
    fun sendToTrackingEntityAndSelf(packet: Any, player: Player) {
        val payload = encodePayload(packet)
        for (p in PlayerLookup.tracking(player)) {
            ServerPlayNetworking.send(p, payload)
        }
        if (player is ServerPlayer) {
            ServerPlayNetworking.send(player, payload)
        }
    }

    @JvmStatic
    fun toClientboundPacket(packet: Any): Packet<*> {
        return ServerPlayNetworking.createS2CPacket(encodePayload(packet))
    }

    @JvmStatic
    fun toServerboundPacket(packet: Any): Packet<*> {
        if (FabricLoader.getInstance().environmentType != EnvType.CLIENT) {
            throw IllegalStateException("toServerboundPacket can only be invoked from the client environment")
        }
        return YSMChannelClientImpl.toServerboundPacket(encodePayload(packet))
    }

    @JvmStatic
    private fun encodePayload(packet: Any): YSMPayload {
        val id = ID_BY_CLASS[packet.javaClass]
            ?: throw IllegalStateException("Packet type not registered: ${packet.javaClass}")
        val buf = FriendlyByteBuf(Unpooled.buffer())
        buf.writeByte(id and 0xff)
        val codec = CODECS_BY_ID[id] ?: throw IllegalStateException("Codec not found for ID: $id")
        codec.encode(packet, buf)
        val arr = ByteArray(buf.readableBytes())
        buf.readBytes(arr)
        return YSMPayload(arr)
    }
}