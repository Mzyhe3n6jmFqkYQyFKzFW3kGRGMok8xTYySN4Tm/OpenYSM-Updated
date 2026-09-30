package rip.ysm.api.client.fabric

import com.mojang.blaze3d.vertex.BufferBuilder
import java.nio.ByteBuffer

object BufferBuilderBridgeImpl {
    @JvmStatic
    fun putBulkData(builder: BufferBuilder, buffer: ByteBuffer): Boolean = false

    @JvmStatic
    fun supportsDirectTransfer(): Boolean = false
}
