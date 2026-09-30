package rip.ysm.api.network.fabric

import net.minecraft.network.Connection
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import rip.ysm.api.network.PacketContext

internal class ServerPacketContext(
    private val server: MinecraftServer,
    private val player: ServerPlayer,
    override val connection: Connection
) : PacketContext {
    override fun isClientSide(): Boolean = false

    override val sender: ServerPlayer
        get() = player

    override fun enqueueWork(runnable: Runnable) {
        server.execute(runnable)
    }
}
