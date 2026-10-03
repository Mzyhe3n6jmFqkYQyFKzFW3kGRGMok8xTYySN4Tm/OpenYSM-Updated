package rip.ysm.gpu

import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel
import com.elfmcys.yesstevemodel.mixin.client.GlBufferAccessor
import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.opengl.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import org.lwjgl.opengl.GL45
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer

class GpuMesh(
    @JvmField val pointer: Long,
    @JvmField val vao: Int,
    @JvmField val vbo: Int,
    @JvmField val ibo: GpuBuffer,
    @JvmField val boneSsbo: Int,
    @JvmField val vertexCount: Int,
    @JvmField val indexCount: Int,
    @JvmField val boneCount: Int,
    @JvmField val partMask1Start: Int,
    @JvmField val partMask1Count: Int,
    @JvmField val partMask2Start: Int,
    @JvmField val partMask2Count: Int,
    @JvmField val partMask3Start: Int,
    @JvmField val partMask3Count: Int
) {
    @JvmField
    val perFrameBoneBuffer: ByteBuffer = MemoryUtil.memAlloc(boneCount * 144)

    private var xformVbo: GpuBuffer? = null
    private var disposed: Boolean = false

    fun indexOffsetBytes(renderPartMask: Int): Int {
        if (renderPartMask == 0 || renderPartMask == 3) return 0
        if (renderPartMask == 1) return partMask1Start * 4
        if (renderPartMask == 2) return partMask2Start * 4
        return 0
    }

    fun indexFirstIndex(renderPartMask: Int): Int {
        if (renderPartMask == 0 || renderPartMask == 3) return 0
        if (renderPartMask == 1) return partMask1Start
        if (renderPartMask == 2) return partMask2Start
        return 0
    }

    fun indexDrawCount(renderPartMask: Int): Int {
        if (renderPartMask == 0 || renderPartMask == 3) return indexCount
        val self = when (renderPartMask) {
            1 -> partMask1Count
            2 -> partMask2Count
            else -> 0
        }
        return self + partMask3Count
    }

    fun iboHandle(): Int {
        return (ibo as GlBufferAccessor).`ysm$getHandle`()
    }

    fun xformVbo(): GpuBuffer? = xformVbo

    fun xformVboHandle(): Int {
        val buffer = xformVbo ?: return 0
        return (buffer as GlBufferAccessor).`ysm$getHandle`()
    }

    fun ensureXformBuffers() {
        if (xformVbo != null) return
        xformVbo = RenderSystem.getDevice().createBuffer(
            { "ysm-xform-vbo" },
            GpuBuffer.USAGE_VERTEX or GpuBuffer.USAGE_COPY_DST,
            vertexCount.toLong() * 36
        )
    }

    fun dispose() {
        if (disposed) return
        disposed = true
        GlStateManager._glDeleteBuffers(vbo)
        ibo.close()
        GlStateManager._glDeleteBuffers(boneSsbo)
        GL45.glDeleteVertexArrays(vao)
        xformVbo?.close()
        if (pointer != 0L) {
            GeoModel.nFreeGpuMesh(pointer)
        }
        MemoryUtil.memFree(perFrameBoneBuffer)
    }
}