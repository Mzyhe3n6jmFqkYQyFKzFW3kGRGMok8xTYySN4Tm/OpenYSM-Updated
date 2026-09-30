package rip.ysm.api.network.fabric

import net.minecraft.network.FriendlyByteBuf
import rip.ysm.api.network.PacketContext
import java.util.function.BiConsumer
import java.util.function.Function

data class Codec<T>(
    val type: Class<T>,
    val encoder: BiConsumer<T, FriendlyByteBuf>,
    val decoder: Function<FriendlyByteBuf, T>,
    val handler: BiConsumer<T, PacketContext>
) {
    fun encode(packet: Any, buf: FriendlyByteBuf) {
        encoder.accept(type.cast(packet), buf)
    }

    fun dispatch(buf: FriendlyByteBuf, ctx: PacketContext) {
        handler.accept(decoder.apply(buf), ctx)
    }
}
