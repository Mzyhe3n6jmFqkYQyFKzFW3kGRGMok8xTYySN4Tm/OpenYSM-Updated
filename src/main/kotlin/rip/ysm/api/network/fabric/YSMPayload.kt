package rip.ysm.api.network.fabric

import io.netty.buffer.Unpooled
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.Identifier

class YSMPayload(val data: ByteArray) : CustomPacketPayload {
    fun toBuf(): FriendlyByteBuf = FriendlyByteBuf(Unpooled.wrappedBuffer(data))

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        @JvmField
        var TYPE: CustomPacketPayload.Type<YSMPayload> =
            CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("yes_steve_model", "main"))

        @JvmField
        var CODEC: StreamCodec<RegistryFriendlyByteBuf, YSMPayload> = StreamCodec.of(
            { buf, payload ->
                buf.writeVarInt(payload.data.size)
                buf.writeBytes(payload.data)
            },
            { buf ->
                val len = buf.readVarInt()
                val arr = ByteArray(len)
                buf.readBytes(arr)
                YSMPayload(arr)
            }
        )

        @JvmStatic
        fun init(channelId: Identifier) {
            TYPE = CustomPacketPayload.Type(channelId)
            CODEC = StreamCodec.of(
                { buf, payload ->
                    buf.writeVarInt(payload.data.size)
                    buf.writeBytes(payload.data)
                },
                { buf ->
                    val len = buf.readVarInt()
                    val arr = ByteArray(len)
                    buf.readBytes(arr)
                    YSMPayload(arr)
                }
            )
        }

        @JvmStatic
        fun fromBuf(buf: FriendlyByteBuf): YSMPayload {
            val readable = buf.readableBytes()
            val arr = ByteArray(readable)
            buf.readBytes(arr)
            return YSMPayload(arr)
        }
    }
}
