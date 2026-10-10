package rip.ysm.api.client

import com.mojang.blaze3d.vertex.BufferBuilder
import rip.ysm.api.client.fabric.BufferBuilderBridgeImpl
import java.nio.ByteBuffer

object BufferBuilderBridge {
    fun putBulkData(builder: BufferBuilder, buffer: ByteBuffer): Boolean =
        BufferBuilderBridgeImpl.putBulkData(builder, buffer)

    fun supportsDirectTransfer(): Boolean = BufferBuilderBridgeImpl.supportsDirectTransfer()
}
