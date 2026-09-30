package rip.ysm.api.network.fabric.client

import net.minecraft.client.Minecraft
import net.minecraft.network.Connection
import net.minecraft.server.level.ServerPlayer
import rip.ysm.api.network.PacketContext

internal class ClientPacketContext(
    private val client: Minecraft,
    override val connection: Connection
) : PacketContext {
    override fun isClientSide(): Boolean = true

    override val sender: ServerPlayer?
        get() = null

    override fun enqueueWork(runnable: Runnable) {
        client.execute(runnable)
    }
}
