package rip.ysm.api.client.fabric

import com.mojang.blaze3d.vertex.BufferBuilder
import java.nio.ByteBuffer

// TODO: What is this?
object BufferBuilderBridgeImpl {
    fun putBulkData(builder: BufferBuilder, buffer: ByteBuffer): Boolean = false

    fun supportsDirectTransfer(): Boolean = false
}
