package rip.ysm.api.network.fabric

import net.minecraft.network.FriendlyByteBuf
import rip.ysm.api.network.PacketContext

data class Codec<T : Any>(
    val type: Class<T>,
    val encoder: (T, FriendlyByteBuf) -> Unit,
    val decoder: (FriendlyByteBuf) -> T,
    val handler: (T, PacketContext) -> Unit
) {
    fun encode(packet: Any, buf: FriendlyByteBuf) {
        encoder(type.cast(packet), buf)
    }

    fun dispatch(buf: FriendlyByteBuf, ctx: PacketContext) {
        handler(decoder(buf), ctx)
    }
}
