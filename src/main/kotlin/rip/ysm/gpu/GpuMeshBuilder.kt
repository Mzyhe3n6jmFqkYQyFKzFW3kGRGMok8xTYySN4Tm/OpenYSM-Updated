package rip.ysm.gpu

import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel
import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.opengl.GlBuffer
import com.mojang.blaze3d.opengl.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import org.lwjgl.opengl.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

object GpuMeshBuilder {
    @JvmStatic
    fun build(model: GeoModel): GpuMesh? {
        val bakedBones = model.bakedBones ?: return null
        if (bakedBones.isEmpty()) return null
        RenderSystem.assertOnRenderThread()

        val modelBuf = serializeModel(model)
        val meta = IntArray(9)
        val handle = GeoModel.nBuildGpuMesh(modelBuf, meta)
        if (handle == 0L) {
            return null
        }
        val vertexCount = meta[0]
        val indexCount = meta[1]
        val boneCount = meta[2]

        val vbuf = GeoModel.nGetGpuMeshVertexBuffer(handle)
        val ibuf = GeoModel.nGetGpuMeshIndexBuffer(handle)
        vbuf.order(ByteOrder.nativeOrder())
        ibuf.order(ByteOrder.nativeOrder())

        val vao = GL30.glGenVertexArrays()
        val vbo = GlStateManager._glGenBuffers()
        val ibo = RenderSystem.getDevice().createBuffer(
            { "ysm-mesh-ibo" },
            GpuBuffer.USAGE_INDEX or GpuBuffer.USAGE_COPY_DST,
            ibuf
        )
        val ssbo = GlStateManager._glGenBuffers()
        val iboHandle = (ibo as GlBuffer).handle

        GL30.glBindVertexArray(vao)
        GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vbuf, GL15.GL_STATIC_DRAW)
        GlStateManager._glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, iboHandle)

        GL20.glEnableVertexAttribArray(0)
        GL20.glVertexAttribPointer(0, 3, GL15.GL_FLOAT, false, 32, 0L)
        GL20.glEnableVertexAttribArray(1)
        GL20.glVertexAttribPointer(1, 2, GL15.GL_FLOAT, false, 32, 12L)
        GL20.glEnableVertexAttribArray(2)
        GL20.glVertexAttribPointer(2, 4, GL33.GL_INT_2_10_10_10_REV, true, 32, 20L)
        GL20.glEnableVertexAttribArray(3)
        GL30.glVertexAttribIPointer(3, 1, GL15.GL_UNSIGNED_SHORT, 32, 24L)
        GL20.glEnableVertexAttribArray(4)
        GL20.glVertexAttribPointer(4, 1, GL11.GL_UNSIGNED_BYTE, false, 32, 27L)

        GL30.glBindVertexArray(0)
        GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
        GlStateManager._glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0)

        GlStateManager._glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, ssbo)
        GL45.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, boneCount.toLong() * 144, GL15.GL_DYNAMIC_DRAW)
        GlStateManager._glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0)
        GeoModel.nReleaseGpuMeshScratch(handle)

        return GpuMesh(
            handle,
            vao,
            vbo,
            ibo,
            ssbo,
            vertexCount,
            indexCount,
            boneCount,
            meta[3],
            meta[4],
            meta[5],
            meta[6],
            meta[7],
            meta[8]
        )
    }

    private fun serializeModel(model: GeoModel): ByteBuffer {
        val bones = model.bakedBones ?: emptyList()
        val totalBones = bones.size
        var totalCubes = 0
        var totalQuads = 0
        for (bone in bones) {
            totalCubes += bone.cubes.size
            for (cube in bone.cubes) {
                totalQuads += cube.quads.size
            }
        }
        val sz = 4 + (totalBones * 25) + (totalCubes * 5) + (totalQuads * 92)
        val buf = ByteBuffer.allocateDirect(sz).order(ByteOrder.nativeOrder())
        buf.putInt(totalBones)
        for (bone in bones) {
            buf.putInt(bone.parentIdx)
            buf.putInt(bone.partMask)
            buf.put(if (bone.glow) 1.toByte() else 0.toByte())
            buf.putFloat(bone.pivotX)
            buf.putFloat(bone.pivotY)
            buf.putFloat(bone.pivotZ)
            buf.putInt(bone.cubes.size)
            for (cube in bone.cubes) {
                buf.put(if (cube.cullable) 1.toByte() else 0.toByte())
                buf.putInt(cube.quads.size)
                for (quad in cube.quads) {
                    for (v in 0 until 4) {
                        buf.putFloat(quad.positions[v].x())
                        buf.putFloat(quad.positions[v].y())
                        buf.putFloat(quad.positions[v].z())
                    }
                    for (v in 0 until 4) {
                        buf.putFloat(quad.uvs[v].x())
                        buf.putFloat(quad.uvs[v].y())
                    }
                    buf.putFloat(quad.normal.x())
                    buf.putFloat(quad.normal.y())
                    buf.putFloat(quad.normal.z())
                }
            }
        }
        buf.position(0)
        return buf
    }
}