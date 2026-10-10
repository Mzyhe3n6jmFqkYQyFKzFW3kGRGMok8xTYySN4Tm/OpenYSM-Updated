package com.elfmcys.yesstevemodel.geckolib3.geo

import com.elfmcys.yesstevemodel.NativeLibLoader
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer
import com.elfmcys.yesstevemodel.config.GeneralConfig
import com.elfmcys.yesstevemodel.extensions.setAndSave
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel
import com.elfmcys.yesstevemodel.util.log.ChatLogger
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LightTexture
import net.minecraft.resources.Identifier
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import rip.ysm.compat.oculus.OculusCompat
import rip.ysm.compat.optifine.OptiFineDetector
import rip.ysm.gpu.GpuCapability
import rip.ysm.gpu.GpuRenderPath
import rip.ysm.gpu.IrisRenderPath
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

object NativeModelRenderer {
    private val projectionModelViewMatrix: Matrix4f = Matrix4f()
    private val matrixTransferArray: FloatArray = FloatArray(48)

    @JvmOverloads
    fun renderMesh(
        buffer: VertexConsumer,
        pose: PoseStack.Pose,
        model: GeoModel,
        boneParams: FloatArray,
        stateBuffer: FloatArray?,
        textureIndex: Int,
        renderPartMask: Int,
        packedLight: Int,
        packedOverlay: Int,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float,
        textureLocation: Identifier? = null
    ) {
        OculusCompat.updatePBRState()
        val isCompatMode = OptiFineDetector.isOptifinePresent() || GeneralConfig.USE_COMPATIBILITY_RENDERER.get()
        projectionModelViewMatrix.set(
            Minecraft.getInstance().gameRenderer.getProjectionMatrix(
                Minecraft.getInstance().options.fov().get().toFloat()
            )
        ).mul(RenderSystem.getModelViewMatrix())
        val isPreview = ModelPreviewRenderer.isPreview || ModelPreviewRenderer.isExtraPlayer()

        if (textureLocation != null && NativeLibLoader.isLoaded && !GeneralConfig.USE_COMPATIBILITY_RENDERER.get() && GeneralConfig.USE_GPU_RENDERER.get()) {
            if (!GpuCapability.isAvailable) {
                ChatLogger.logFormatted("Disabled GPU renderer for: " + GpuCapability.reason)
                GeneralConfig.USE_GPU_RENDERER.setAndSave(false)
                return
            }
            if (OculusCompat.isShaderPackInUse() && !isPreview) {
                if (IrisRenderPath.tryRender(
                        model,
                        pose,
                        boneParams,
                        renderPartMask,
                        packedLight,
                        packedOverlay,
                        red,
                        green,
                        blue,
                        alpha,
                        textureLocation
                    )
                ) return
            } else {
                if (GpuRenderPath.tryRender(
                        model,
                        pose,
                        boneParams,
                        stateBuffer,
                        renderPartMask,
                        packedLight,
                        packedOverlay,
                        red,
                        green,
                        blue,
                        alpha,
                        textureLocation
                    )
                ) return
            }
        }

        if (NativeLibLoader.isLoaded && !GeneralConfig.USE_COMPATIBILITY_RENDERER.get() && !isPreview) {
            nativeRenderModel(
                buffer,
                pose,
                projectionModelViewMatrix,
                OptiFineDetector.isOptifinePresent(),
                model,
                boneParams,
                stateBuffer,
                textureIndex,
                renderPartMask,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha,
                isPreview
            )
        } else {
            renderModel(
                buffer,
                pose,
                projectionModelViewMatrix,
                OptiFineDetector.isOptifinePresent(),
                model,
                boneParams,
                stateBuffer,
                textureIndex,
                renderPartMask,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha,
                isPreview
            )
        }
    }

    fun renderModel(
        vertexConsumer: VertexConsumer,
        pose: PoseStack.Pose,
        projectionModelViewMatrix: Matrix4f,
        isCompatMode: Boolean,
        mesh: GeoModel,
        boneParams: FloatArray,
        stateBuffer: FloatArray?,
        textureIndex: Int,
        renderPartMask: Int,
        packedLight: Int,
        packedOverlay: Int,
        r: Float,
        g: Float,
        b: Float,
        a: Float,
        isPreview: Boolean
    ) {
        val bakedBones = mesh.bakedBones
        if (bakedBones.isNullOrEmpty()) {
            return
        }

        val rootPoseMat = pose.pose()
        val rootNormalMC = pose.normal()
        val projMat = Minecraft.getInstance().gameRenderer.getProjectionMatrix(
            Minecraft.getInstance().options.fov().get().toFloat()
        )

        val identityMat = Matrix4f()
        val globalBoneMat = Matrix4f()
        val projBoneMat = Matrix4f()
        val localNormalMat = Matrix3f()
        val globalNormalMat = Matrix3f()

        val tempPos = Vector4f()
        val tempNorm = Vector3f()
        val boneCount = bakedBones.size
        val boneLocalTransforms = arrayOfNulls<Matrix4f>(boneCount)
        val boneVisible = BooleanArray(boneCount)

        for (i in 0 until boneCount) {
            calculateBoneMatrix(i, bakedBones, boneParams, boneLocalTransforms, boneVisible, identityMat, stateBuffer)
        }

        for (i in 0 until boneCount) {
            if (!boneVisible[i]) {
                continue
            }
            val bone = bakedBones[i]
            if (renderPartMask != 0 && bone.partMask != renderPartMask && bone.partMask != 3) {
                continue
            }
            val localBoneMat = boneLocalTransforms[i] ?: continue
            globalBoneMat.set(rootPoseMat).mul(localBoneMat)
            projBoneMat.set(projMat).mul(globalBoneMat)
            localBoneMat.normal(localNormalMat)
            globalNormalMat.set(rootNormalMC).mul(localNormalMat)
            val currentPackedLight = if (bone.glow) LightTexture.pack(15, 15) else packedLight
            for (cube in bone.cubes) {
                for (quad in cube.quads) {
                    tempNorm.set(quad.normal).mul(globalNormalMat).normalize()
                    for (v in 0 until 4) {
                        tempPos.set(quad.positions[v].x(), quad.positions[v].y(), quad.positions[v].z(), 1.0f)
                            .mul(globalBoneMat)
                        vertexConsumer.addVertex(tempPos.x(), tempPos.y(), tempPos.z())
                            .setColor(r, g, b, a)
                            .setUv(quad.uvs[v].x(), quad.uvs[v].y())
                            .setOverlay(packedOverlay)
                            .setLight(currentPackedLight)
                            .setNormal(tempNorm.x(), tempNorm.y(), tempNorm.z())
                    }
                }
            }
        }
    }

    private fun calculateBoneMatrix(
        idx: Int,
        bones: List<GeoModel.BakedBone>,
        boneParams: FloatArray,
        cache: Array<Matrix4f?>,
        visibleCache: BooleanArray,
        rootPose: Matrix4f,
        stateBuffer: FloatArray?
    ): Matrix4f {
        val cached = cache[idx]
        if (cached != null) {
            return cached
        }
        val bone = bones[idx]
        var parentMatrix = rootPose
        var isVisible = true
        if (bone.parentIdx != -1) {
            parentMatrix =
                calculateBoneMatrix(bone.parentIdx, bones, boneParams, cache, visibleCache, rootPose, stateBuffer)
            if (!visibleCache[bone.parentIdx]) {
                isVisible = false
            }
        }
        val localMat = Matrix4f(parentMatrix)
        val pOffset = idx * 12
        val animRx = boneParams[pOffset]
        val animRy = boneParams[pOffset + 1]
        val animRz = boneParams[pOffset + 2]
        val animTx = boneParams[pOffset + 3]
        val animTy = boneParams[pOffset + 4]
        val animTz = boneParams[pOffset + 5]
        val animSx = boneParams[pOffset + 6]
        val animSy = boneParams[pOffset + 7]
        val animSz = boneParams[pOffset + 8]
        val unk1 = boneParams[pOffset + 9]
        val unk2 = boneParams[pOffset + 10]
        val unk3 = boneParams[pOffset + 11]

        if (unk1 != 0.0f && unk2 != 0.0f && unk3 != 0.0f) {
        }
        if (animSx == 0.0f && animSy == 0.0f && animSz == 0.0f) {
            isVisible = false
        }
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
        if (unk3 == 1.0f && stateBuffer != null && isVisible) {
            val offset = idx * 4
            if (offset + 2 < stateBuffer.size) {
                stateBuffer[offset + 0] = -localMat.m30() * 16
                stateBuffer[offset + 1] = localMat.m31() * 16
                stateBuffer[offset + 2] = localMat.m32() * 16
            }
        }
        localMat.translate(-bone.pivotX / 16f, -bone.pivotY / 16f, -bone.pivotZ / 16f)
        cache[idx] = localMat
        visibleCache[idx] = isVisible
        return localMat
    }

    @JvmStatic
    fun submitVertices(v: Any, vertexCount: Int, fBuf: ByteBuffer, iBuf: ByteBuffer) {
        val f: FloatBuffer = fBuf.order(ByteOrder.nativeOrder()).asFloatBuffer()
        val inBuf: IntBuffer = iBuf.order(ByteOrder.nativeOrder()).asIntBuffer()
        var fIdx = 0
        var iIdx = 0
        for (i in 0 until vertexCount) {
            (v as VertexConsumer).addVertex(f.get(fIdx), f.get(fIdx + 1), f.get(fIdx + 2))
                .setColor(f.get(fIdx + 3), f.get(fIdx + 4), f.get(fIdx + 5), f.get(fIdx + 6))
                .setUv(f.get(fIdx + 7), f.get(fIdx + 8))
                .setOverlay(inBuf.get(iIdx))
                .setLight(inBuf.get(iIdx + 1))
                .setNormal(f.get(fIdx + 9), f.get(fIdx + 10), f.get(fIdx + 11))
            fIdx += 12
            iIdx += 2
        }
    }

    fun nativeRenderModel(
        vertexConsumer: VertexConsumer,
        pose: PoseStack.Pose,
        projectionModelViewMatrix: Matrix4f,
        isCompatMode: Boolean,
        mesh: GeoModel,
        boneVertex: FloatArray,
        stateBuffer: FloatArray?,
        textureIndex: Int,
        renderPartMask: Int,
        packedLight: Int,
        packedOverlay: Int,
        r: Float,
        g: Float,
        b: Float,
        a: Float,
        isPreview: Boolean
    ) {
        if (mesh.nativeModelHandle == 0L) return
        pose.pose().get(matrixTransferArray, 0)
        pose.normal().get(matrixTransferArray, 16)
        projectionModelViewMatrix.get(matrixTransferArray, 32)
        GeoModel.nComputeModelVertices(
            mesh.nativeModelHandle,
            vertexConsumer,
            matrixTransferArray,
            boneVertex,
            renderPartMask,
            packedLight,
            packedOverlay,
            r,
            g,
            b,
            a
        )
    }
}