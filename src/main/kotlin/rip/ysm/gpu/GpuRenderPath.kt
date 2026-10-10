@file:Suppress("unused")

package rip.ysm.gpu

import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel
import com.mojang.blaze3d.buffers.GpuBufferSlice
import com.mojang.blaze3d.opengl.*
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.FilterMode
import com.mojang.blaze3d.textures.GpuTextureView
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.lwjgl.opengl.*
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

object GpuRenderPath {
    private val rootPoseScratch: FloatArray = FloatArray(16)
    private val rootNormalScratch: FloatArray = FloatArray(9)
    private val modelViewScratch: FloatArray = FloatArray(16)
    private val meshMap: ConcurrentHashMap<Long, GpuMesh> = ConcurrentHashMap()
    private val ref: AtomicLong = AtomicLong(1)
    private val pivotAbsScratchMat: Matrix4f = Matrix4f()
    private var pivotAbsPathScratch: IntArray = IntArray(64)

    fun tryRender(
        model: GeoModel,
        pose: PoseStack.Pose,
        boneParams: FloatArray,
        stateBuffer: FloatArray?,
        renderPartMask: Int,
        packedLight: Int,
        packedOverlay: Int,
        r: Float,
        g: Float,
        b: Float,
        a: Float,
        textureLocation: Identifier
    ): Boolean {
        if (!GpuCapability.isAvailable) return false
        if (!BoneSkinShader.ensureCompiled()) return false
        val bakedBones = model.bakedBones ?: return false
        if (bakedBones.isEmpty()) return false

        if (model.gpuMeshHandle == 0L) {
            val createdMesh = GpuMeshBuilder.build(model) ?: return false
            model.gpuMeshHandle = encodeMeshRef(createdMesh)
        }
        val mesh = decodeMeshRef(model.gpuMeshHandle) ?: return false

        val mc = Minecraft.getInstance()
        val rootPose: Matrix4f = pose.pose()
        val rootNormal: Matrix3f = pose.normal()
        rootPose.get(rootPoseScratch)
        rootNormal.get(rootNormalScratch)
        RenderSystem.getModelViewMatrix().get(modelViewScratch)

        val boneBuf: ByteBuffer = mesh.perFrameBoneBuffer
        boneBuf.clear()
        updatePivotAbsStateBuffer(model, boneParams, stateBuffer)
        GeoModel.nComputeBoneMatrices(
            mesh.pointer,
            rootPoseScratch,
            rootNormalScratch,
            boneParams,
            packedLight,
            boneBuf
        )
        boneBuf.position(0)
        boneBuf.limit(mesh.boneCount * 144)

        val savedProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM)
        val savedFbo = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING)

        val targetColorView: GpuTextureView?
        val targetDepthView: GpuTextureView?
        val targetWidth: Int
        val targetHeight: Int
        if (RenderSystem.outputColorTextureOverride != null && RenderSystem.outputDepthTextureOverride != null) {
            targetColorView = RenderSystem.outputColorTextureOverride
            targetDepthView = RenderSystem.outputDepthTextureOverride
            targetWidth = targetColorView?.getWidth(0) ?: 0
            targetHeight = targetColorView?.getHeight(0) ?: 0
        } else {
            val mainTarget: RenderTarget = mc.mainRenderTarget
            targetColorView = mainTarget.colorTextureView
            targetDepthView = mainTarget.depthTextureView
            targetWidth = mainTarget.width
            targetHeight = mainTarget.height
        }
        if (targetColorView !is GlTextureView || targetDepthView !is GlTextureView) {
            return false
        }
        val dsa: DirectStateAccess = (RenderSystem.getDevice() as GlDevice).directStateAccess()
        val targetFbo = targetColorView.getFbo(dsa, targetDepthView.texture())
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, targetFbo)
        GlStateManager._viewport(0, 0, targetWidth, targetHeight)

        GlStateManager._disableCull()
        GlStateManager._enableDepthTest()
        GlStateManager._depthFunc(GL11.GL_LEQUAL)
        GlStateManager._depthMask(true)
        GlStateManager._disableBlend()

        val modelTex = mc.textureManager.getTexture(textureLocation)
        val modelGpuTex = modelTex.texture
        if (modelGpuTex !is GlTexture) {
            return false
        }
        val modelTexId = modelGpuTex.glId()

        val lightView = mc.gameRenderer.lightTexture().textureView
        if (lightView !is GlTextureView) {
            return false
        }
        val lightTexId = lightView.texture().glId()

        val overlayView = mc.gameRenderer.overlayTexture().textureView
        if (overlayView !is GlTextureView) {
            return false
        }
        val overlayTexId = overlayView.texture().glId()

        val nearestSamplerId = (RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST) as GlSampler).id
        val linearSamplerId = (RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR) as GlSampler).id

        GlStateManager._activeTexture(GL13.GL_TEXTURE0 + 2)
        GL33C.glBindSampler(2, linearSamplerId)
        GlStateManager._bindTexture(lightTexId)

        GlStateManager._activeTexture(GL13.GL_TEXTURE0 + 1)
        GL33C.glBindSampler(1, nearestSamplerId)
        GlStateManager._bindTexture(overlayTexId)

        GlStateManager._activeTexture(GL13.GL_TEXTURE0)
        GL33C.glBindSampler(0, nearestSamplerId)
        GlStateManager._bindTexture(modelTexId)

        GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, mesh.boneSsbo)
        GL15.glBufferSubData(GL43.GL_SHADER_STORAGE_BUFFER, 0L, boneBuf)
        GL43.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, BoneSkinShader.SSBO, mesh.boneSsbo)

        GlStateManager._glUseProgram(BoneSkinShader.program())
        if (BoneSkinShader.locModelView() >= 0) {
            GL20.glUniformMatrix4fv(BoneSkinShader.locModelView(), false, modelViewScratch)
        }
        if (BoneSkinShader.locColor() >= 0) {
            GL20.glUniform4f(BoneSkinShader.locColor(), r, g, b, a)
        }
        if (BoneSkinShader.locOverlay() >= 0) {
            GL20.glUniform1i(BoneSkinShader.locOverlay(), packedOverlay)
        }
        if (BoneSkinShader.locFogStart() >= 0) {
            GL20.glUniform1f(BoneSkinShader.locFogStart(), 0.0f)
        }
        if (BoneSkinShader.locFogEnd() >= 0) {
            GL20.glUniform1f(BoneSkinShader.locFogEnd(), Float.MAX_VALUE)
        }
        if (BoneSkinShader.locFogColor() >= 0) {
            GL20.glUniform4f(BoneSkinShader.locFogColor(), 0.0f, 0.0f, 0.0f, 0.0f)
        }
        if (BoneSkinShader.locFogShape() >= 0) {
            GL20.glUniform1i(BoneSkinShader.locFogShape(), 0)
        }

        val lightsSlice: GpuBufferSlice? = RenderSystem.getShaderLights()
        val lightsBuf = lightsSlice?.buffer()
        if (lightsBuf is GlBuffer) {
            val lightsHandle = lightsBuf.handle
            GL30.glBindBufferRange(
                GL31.GL_UNIFORM_BUFFER,
                BoneSkinShader.LIGHT_UBO_BINDING,
                lightsHandle,
                lightsSlice.offset(),
                lightsSlice.length()
            )
        }

        val projSlice: GpuBufferSlice? = RenderSystem.getProjectionMatrixBuffer()
        val projBuf = projSlice?.buffer()
        if (projBuf is GlBuffer) {
            val projHandle = projBuf.handle
            GL30.glBindBufferRange(
                GL31.GL_UNIFORM_BUFFER,
                BoneSkinShader.PROJ_UBO_BINDING,
                projHandle,
                projSlice.offset(),
                projSlice.length()
            )
        }

        GlStateManager._glBindVertexArray(mesh.vao)
        val offsetBytes = mesh.indexOffsetBytes(renderPartMask)
        val drawCount = mesh.indexDrawCount(renderPartMask)
        if (drawCount > 0) {
            if (BoneSkinShader.locAlphaMode() >= 0) {
                GL20.glUniform1i(BoneSkinShader.locAlphaMode(), 1)
            }
            GL11.glDrawElements(GL11.GL_TRIANGLES, drawCount, GL11.GL_UNSIGNED_INT, offsetBytes.toLong())
            GlStateManager._enableBlend()
            GlStateManager._blendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ONE_MINUS_SRC_ALPHA
            )
            if (BoneSkinShader.locAlphaMode() >= 0) {
                GL20.glUniform1i(BoneSkinShader.locAlphaMode(), 2)
            }
            GL11.glDrawElements(GL11.GL_TRIANGLES, drawCount, GL11.GL_UNSIGNED_INT, offsetBytes.toLong())
            GlStateManager._disableBlend()
        }

        GL43.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, BoneSkinShader.SSBO, 0)
        GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0)
        GL30.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, BoneSkinShader.LIGHT_UBO_BINDING, 0)
        GL30.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, BoneSkinShader.PROJ_UBO_BINDING, 0)
        GlStateManager._glUseProgram(savedProgram)
        GlStateManager._glBindVertexArray(0)
        GlStateManager._activeTexture(GL13.GL_TEXTURE0 + 2)
        GL33C.glBindSampler(2, 0)
        GlStateManager._bindTexture(0)
        GlStateManager._activeTexture(GL13.GL_TEXTURE0 + 1)
        GL33C.glBindSampler(1, 0)
        GlStateManager._bindTexture(0)
        GlStateManager._activeTexture(GL13.GL_TEXTURE0)
        GL33C.glBindSampler(0, 0)
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, savedFbo)
        return true
    }

    fun disposeMesh(model: GeoModel) {
        if (model.gpuMeshHandle == 0L) return
        val mesh = meshMap.remove(model.gpuMeshHandle)
        mesh?.dispose()
        model.gpuMeshHandle = 0L
    }

    fun getOrBuildMesh(model: GeoModel): GpuMesh? {
        if (model.gpuMeshHandle == 0L) {
            val mesh = GpuMeshBuilder.build(model) ?: return null
            model.gpuMeshHandle = encodeMeshRef(mesh)
        }
        return decodeMeshRef(model.gpuMeshHandle)
    }

    fun encodeMeshRef(mesh: GpuMesh): Long {
        val nextRef = ref.getAndIncrement()
        meshMap[nextRef] = mesh
        return nextRef
    }

    fun decodeMeshRef(meshRef: Long): GpuMesh? {
        return meshMap[meshRef]
    }

    fun updatePivotAbsStateBuffer(model: GeoModel, boneParams: FloatArray?, stateBuffer: FloatArray?) {
        if (stateBuffer == null || boneParams == null) return
        val bones = model.bakedBones ?: return
        if (bones.isEmpty()) return

        val boneCount = bones.size
        for (i in 0 until boneCount) {
            val pOffset = i * 12
            if (pOffset + 11 >= boneParams.size) break
            val unk3 = boneParams[pOffset + 11]
            if (unk3 != 1.0f) continue
            val sOffset = i * 4
            if (sOffset + 2 >= stateBuffer.size) continue
            computeOnePivotAbs(i, bones, boneParams, stateBuffer, sOffset)
        }
    }

    private fun computeOnePivotAbs(
        targetIdx: Int,
        bones: List<GeoModel.BakedBone>,
        boneParams: FloatArray,
        stateBuffer: FloatArray,
        stateOffset: Int
    ) {
        var depth = 0
        var idx = targetIdx
        while (idx != -1) {
            if (depth >= pivotAbsPathScratch.size) {
                val newPath = IntArray(pivotAbsPathScratch.size * 2)
                System.arraycopy(pivotAbsPathScratch, 0, newPath, 0, pivotAbsPathScratch.size)
                pivotAbsPathScratch = newPath
            }
            pivotAbsPathScratch[depth++] = idx
            idx = bones[idx].parentIdx
        }
        val localMat = pivotAbsScratchMat.identity()
        var isVisible = true
        for (p in depth - 1 downTo 0) {
            val boneIdx = pivotAbsPathScratch[p]
            val bone = bones[boneIdx]
            val pOffset = boneIdx * 12
            if (pOffset + 11 >= boneParams.size) return
            val animRx = boneParams[pOffset]
            val animRy = boneParams[pOffset + 1]
            val animRz = boneParams[pOffset + 2]
            val animTx = boneParams[pOffset + 3]
            val animTy = boneParams[pOffset + 4]
            val animTz = boneParams[pOffset + 5]
            val animSx = boneParams[pOffset + 6]
            val animSy = boneParams[pOffset + 7]
            val animSz = boneParams[pOffset + 8]
            if (animSx == 0.0f && animSy == 0.0f && animSz == 0.0f) {
                isVisible = false
            }
            if (!isVisible) return

            localMat.translate(
                (bone.pivotX - animTx) * 0.0625f,
                (bone.pivotY + animTy) * 0.0625f,
                (bone.pivotZ + animTz) * 0.0625f
            )
            localMat.rotateZ(animRz)
            localMat.rotateY(animRy)
            localMat.rotateX(animRx)
            if (animSx != 1.0f || animSy != 1.0f || animSz != 1.0f) {
                localMat.scale(animSx, animSy, animSz)
            }
            if (boneIdx == targetIdx) {
                stateBuffer[stateOffset] = -localMat.m30() * 16.0f
                stateBuffer[stateOffset + 1] = localMat.m31() * 16.0f
                stateBuffer[stateOffset + 2] = localMat.m32() * 16.0f
                return
            }
            localMat.translate(-bone.pivotX / 16.0f, -bone.pivotY / 16.0f, -bone.pivotZ / 16.0f)
        }
    }
}