package com.elfmcys.yesstevemodel.geckolib3.geo.render.built

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel
import com.elfmcys.yesstevemodel.resource.models.GeometryDescription
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.ints.IntList
import it.unimi.dsi.fastutil.ints.IntLists
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectLists
import org.joml.Vector2f
import org.joml.Vector3f
import rip.ysm.gpu.GpuRenderPath
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Bedrock的.geo模型文件
 */
open class GeoModel(
    geoBones: Array<GeoBone>,
    strArr: Array<Array<String>>,
    zArr: BooleanArray,
    @JvmField val properties: GeometryDescription,
    zArr2: BooleanArray,
) {
    @JvmField
    val bones: List<GeoBone> = ObjectLists.unmodifiable(ObjectArrayList.wrap(geoBones))

    @JvmField
    val leftHandIds: IntList = resolveBoneIds(strArr[0])

    @JvmField
    val rightHandIds: IntList = resolveBoneIds(strArr[1])

    @JvmField
    val elytraIds: IntList = resolveBoneIds(strArr[2])

    @JvmField
    val tacPistolIds: IntList = resolveBoneIds(strArr[3])

    @JvmField
    val tacRifleIds: IntList = resolveBoneIds(strArr[4])

    @JvmField
    val leftWaistIds: IntList = resolveBoneIds(strArr[5])

    @JvmField
    val rightWaistIds: IntList = resolveBoneIds(strArr[6])

    @JvmField
    val leftShoulderIds: IntList = resolveBoneIds(strArr[7])

    @JvmField
    val rightShoulderIds: IntList = resolveBoneIds(strArr[8])

    @JvmField
    val bladeIds: IntList = resolveBoneIds(strArr[9])

    @JvmField
    val sheathIds: IntList = resolveBoneIds(strArr[10])

    @JvmField
    val headIds: IntList = resolveBoneIds(strArr[11])

    @JvmField
    val backpackIds: IntList = resolveBoneIds(strArr[12])

    @JvmField
    val hasCustomLeftHand: Boolean = zArr[0]

    @JvmField
    val hasCustomRightHand: Boolean = zArr[1]

    @JvmField
    val hasCustomLimbs: Boolean = zArr[2]

    @JvmField
    val boneTransformData: FloatArray

    private var translucentTexture: BooleanArray = zArr2

    @JvmField
    val extraLeftHandGroups: MutableList<IntList> = ObjectArrayList()

    @JvmField
    val extraRightHandGroups: MutableList<IntList> = ObjectArrayList()

    @JvmField
    val passengerGroups: MutableList<IntList> = ObjectArrayList()

    @JvmField
    var bakedBones: MutableList<BakedBone>? = null

    @JvmField
    var nativeModelHandle: Long = 0L

    @JvmField
    var gpuMeshHandle: Long = 0L

    init {
        for (i in 13..19) {
            val strArr2 = strArr[i]
            if (strArr2.isNotEmpty()) {
                extraLeftHandGroups.add(resolveBoneIds(strArr2))
            }
        }
        for (i in 20..26) {
            val strArr3 = strArr[i]
            if (strArr3.isNotEmpty()) {
                extraRightHandGroups.add(resolveBoneIds(strArr3))
            }
        }
        for (i in 27..34) {
            val strArr4 = strArr[i]
            if (strArr4.isNotEmpty()) {
                passengerGroups.add(resolveBoneIds(strArr4))
            }
        }
        boneTransformData = AnimatedGeoModel(this).getMatrixData()
    }

    open fun buildNativeCache() {
        val bones = bakedBones
        if (bones.isNullOrEmpty()) return

        val totalBones = bones.size
        var totalCubes = 0
        var totalQuads = 0

        for (bone in bones) {
            totalCubes += bone.cubes.size
            for (cube in bone.cubes) {
                totalQuads += cube.quads.size
            }
        }

        val initBufferSize = 4 + (totalBones * 25) + (totalCubes * 5) + (totalQuads * 92)
        val buffer = ByteBuffer.allocateDirect(initBufferSize).order(ByteOrder.nativeOrder())

        buffer.putInt(bones.size)
        for (bone in bones) {
            buffer.putInt(bone.parentIdx)
            buffer.putInt(bone.partMask)
            buffer.put(if (bone.glow) 1.toByte() else 0.toByte())
            buffer.putFloat(bone.pivotX)
            buffer.putFloat(bone.pivotY)
            buffer.putFloat(bone.pivotZ)

            buffer.putInt(bone.cubes.size)
            for (cube in bone.cubes) {
                buffer.put(if (cube.cullable) 1.toByte() else 0.toByte())
                buffer.putInt(cube.quads.size)
                for (quad in cube.quads) {
                    val positions = quad.positions
                    for (v in 0 until 4) {
                        val pos = positions[v]
                        buffer.putFloat(pos.x())
                        buffer.putFloat(pos.y())
                        buffer.putFloat(pos.z())
                    }
                    val uvs = quad.uvs
                    for (v in 0 until 4) {
                        val uv = uvs[v]
                        buffer.putFloat(uv.x())
                        buffer.putFloat(uv.y())
                    }
                    val normal = quad.normal
                    buffer.putFloat(normal.x())
                    buffer.putFloat(normal.y())
                    buffer.putFloat(normal.z())
                }
            }
        }

        buffer.position(0)
        nativeModelHandle = nInitModelCache(buffer)
    }

    open fun freeNativeCache() {
        if (nativeModelHandle != 0L) {
            nDestroyModelCache(nativeModelHandle)
            nativeModelHandle = 0L
        }
        if (gpuMeshHandle != 0L) {
            GpuRenderPath.disposeMesh(this)
        }
    }

    open fun topLevelBones(): List<GeoBone> = bones

    open fun getBoneTransformData(): FloatArray = boneTransformData

    open fun getProperties(): GeometryDescription = properties

    open fun isTranslucentTexture(i: Int): Boolean {
        return !(i < 0 || i >= translucentTexture.size) && translucentTexture[i]
    }

    open fun setTranslucentTexture(i: Int, translucent: Boolean) {
        ensureTranslucentTextureCapacity(i)
        translucentTexture[i] = translucent
    }

    private fun ensureTranslucentTextureCapacity(maxIndex: Int) {
        if (maxIndex >= translucentTexture.size) {
            val expanded = BooleanArray(maxIndex + 1)
            System.arraycopy(translucentTexture, 0, expanded, 0, translucentTexture.size)
            translucentTexture = expanded
        }
    }

    open class BakedBone {
        @JvmField
        var name: String = ""

        @JvmField
        var glow: Boolean = false

        @JvmField
        var parentIdx: Int = -1

        @JvmField
        var pivotX: Float = 0f

        @JvmField
        var pivotY: Float = 0f

        @JvmField
        var pivotZ: Float = 0f

        @JvmField
        var rotX: Float = 0f

        @JvmField
        var rotY: Float = 0f

        @JvmField
        var rotZ: Float = 0f

        @JvmField
        var cubes: MutableList<BakedCube> = ObjectArrayList()

        @JvmField
        var partMask: Int = 0
    }

    open class BakedCube {
        @JvmField
        var cullable: Boolean = false

        @JvmField
        var quads: MutableList<BakedQuad> = ObjectArrayList()
    }

    open class BakedQuad {
        @JvmField
        var positions: Array<Vector3f> = Array(4) { Vector3f() }

        @JvmField
        var uvs: Array<Vector2f> = Array(4) { Vector2f() }

        @JvmField
        var normal: Vector3f = Vector3f()
    }

    companion object {
        @JvmStatic
        external fun nInitModelCache(buffer: ByteBuffer): Long

        @JvmStatic
        external fun nDestroyModelCache(handle: Long)

        @JvmStatic
        external fun nComputeModelVertices(
            handle: Long, vertexConsumer: Any,
            matrixTransfer: FloatArray, animTransfer: FloatArray,
            renderPartMask: Int, packedLight: Int, packedOverlay: Int,
            r: Float, g: Float, b: Float, a: Float
        )

        @JvmStatic
        external fun nBuildGpuMesh(buffer: ByteBuffer, outMeta: IntArray): Long

        @JvmStatic
        external fun nGetGpuMeshVertexBuffer(pointer: Long): ByteBuffer

        @JvmStatic
        external fun nGetGpuMeshIndexBuffer(pointer: Long): ByteBuffer

        @JvmStatic
        external fun nReleaseGpuMeshScratch(pointer: Long)

        @JvmStatic
        external fun nFreeGpuMesh(pointer: Long)

        @JvmStatic
        external fun nComputeBoneMatrices(
            pointer: Long,
            rootPose: FloatArray,
            rootNormal: FloatArray,
            anim: FloatArray,
            packedLight: Int,
            outBoneBuffer: ByteBuffer
        )

        @JvmStatic
        external fun nComputeBoneMatricesLocal(
            handle: Long,
            animArray: FloatArray,
            packedLight: Int,
            outBoneBuffer: ByteBuffer
        )

        @JvmStatic
        fun resolveBoneIds(strArr: Array<String>): IntList {
            val intArrayList = IntArrayList(strArr.size)
            for (str in strArr) intArrayList.add(StringPool.computeIfAbsent(str))
            return IntLists.unmodifiable(intArrayList)
        }
    }
}