package rip.ysm.api.network

import net.minecraft.network.Connection
import net.minecraft.server.level.ServerPlayer

interface PacketContext {
    fun isClientSide(): Boolean

    fun isServerSide(): Boolean = !isClientSide()

    val sender: ServerPlayer?

    val connection: Connection

    fun enqueueWork(runnable: Runnable)
}
