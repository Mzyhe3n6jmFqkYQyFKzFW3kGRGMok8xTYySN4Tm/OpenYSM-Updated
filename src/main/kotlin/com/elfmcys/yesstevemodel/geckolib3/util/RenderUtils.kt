@file:Suppress("unused")

package com.elfmcys.yesstevemodel.geckolib3.util

import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoBone
import com.mojang.blaze3d.vertex.PoseStack
import org.joml.Matrix4f
import org.joml.Quaternionf

object RenderUtils {
    @JvmStatic
    fun translateMatrixToBone(poseStack: PoseStack, bone: IBone) {
        poseStack.translate(-bone.getPositionX() / 16.0f, bone.getPositionY() / 16.0f, bone.getPositionZ() / 16.0f)
    }

    @JvmStatic
    fun rotateMatrixAroundBone(poseStack: PoseStack, bone: IBone) {
        if (bone.getRotationZ() != 0.0f || bone.getRotationY() != 0.0f || bone.getRotationX() != 0.0f) {
            poseStack.mulPose(Quaternionf().rotateZYX(bone.getRotationZ(), bone.getRotationY(), bone.getRotationX()))
        }
    }

    @JvmStatic
    fun scaleMatrixForBone(poseStack: PoseStack, bone: IBone): Boolean {
        val scaleX = bone.getScaleX()
        val scaleY = bone.getScaleY()
        val scaleZ = bone.getScaleZ()
        poseStack.scale(scaleX, scaleY, scaleZ)
        return scaleX == 0.0f && scaleY == 0.0f && scaleZ == 0.0f
    }

    @JvmStatic
    fun translateToPivotPoint(poseStack: PoseStack, bone: IBone) {
        poseStack.translate(bone.getPivotX() / 16.0f, bone.getPivotY() / 16.0f, bone.getPivotZ() / 16.0f)
    }

    @JvmStatic
    fun translateAwayFromPivotPoint(poseStack: PoseStack, bone: IBone) {
        poseStack.translate(-bone.getPivotX() / 16.0f, -bone.getPivotY() / 16.0f, -bone.getPivotZ() / 16.0f)
    }

    @JvmStatic
    fun translateAndRotateMatrixForBone(poseStack: PoseStack, bone: IBone) {
        translateToPivotPoint(poseStack, bone)
        rotateMatrixAroundBone(poseStack, bone)
    }

    @JvmStatic
    fun prepMatrixForBone(poseStack: PoseStack, bone: IBone): Boolean {
        translateMatrixToBone(poseStack, bone)
        translateToPivotPoint(poseStack, bone)
        rotateMatrixAroundBone(poseStack, bone)
        val scaleMatrixForBone = scaleMatrixForBone(poseStack, bone)
        translateAwayFromPivotPoint(poseStack, bone)
        return scaleMatrixForBone
    }

    @JvmStatic
    fun prepMatrixForLocator(poseStack: PoseStack, locatorHierarchy: List<IBone>): Boolean {
        var scaleCheck = false
        for (i in 0 until locatorHierarchy.size - 1) {
            val result = prepMatrixForBone(poseStack, locatorHierarchy[i])
            if (result) {
                scaleCheck = true
            }
        }
        val lastBone = locatorHierarchy[locatorHierarchy.size - 1]
        translateMatrixToBone(poseStack, lastBone)
        translateToPivotPoint(poseStack, lastBone)
        rotateMatrixAroundBone(poseStack, lastBone)
        scaleMatrixForBone(poseStack, lastBone)
        return scaleCheck
    }

    @JvmStatic
    fun invertAndMultiplyMatrices(baseMatrix: Matrix4f, inputMatrix: Matrix4f): Matrix4f {
        val resultMatrix = Matrix4f(inputMatrix)
        resultMatrix.invert()
        resultMatrix.mul(baseMatrix)
        return resultMatrix
    }

    @JvmStatic
    fun updateMatrices(bones: List<GeoBone>, boneParams: FloatArray, rootPose: Matrix4f): Array<Matrix4f> {
        val boneCount = bones.size
        val poses = Array(boneCount) { Matrix4f() }
        for (i in 0 until boneCount) {
            val bone = bones[i]
            val currentMatrix = Matrix4f(if (bone.parentIdx == -1) rootPose else poses[bone.parentIdx])
            poses[i] = prepMatrixForBone(bones[i], currentMatrix, boneParams, i * 12)
        }
        return poses
    }

    @JvmStatic
    fun prepMatrixForBone(bone: GeoBone, pose: Matrix4f, boneParams: FloatArray, boneIdx: Int): Matrix4f {
        val rotX = boneParams[boneIdx]
        val rotY = boneParams[boneIdx + 1]
        val rotZ = boneParams[boneIdx + 2]
        val posX = boneParams[boneIdx + 3]
        val posY = boneParams[boneIdx + 4]
        val posZ = boneParams[boneIdx + 5]
        val scaleX = boneParams[boneIdx + 6]
        val scaleY = boneParams[boneIdx + 7]
        val scaleZ = boneParams[boneIdx + 8]
        val pivotX = bone.pivotX
        val pivotY = bone.pivotY
        val pivotZ = bone.pivotZ
        pose.translate(-posX / 16.0f, posY / 16.0f, posZ / 16.0f)
        pose.translate(pivotX / 16.0f, pivotY / 16.0f, pivotZ / 16.0f)
        if (rotZ != 0.0f) {
            pose.rotateZ(rotZ)
        }
        if (rotY != 0.0f) {
            pose.rotateY(rotY)
        }
        if (rotX != 0.0f) {
            pose.rotateX(rotX)
        }
        if (scaleX != 1.0f || scaleY != 1.0f || scaleZ != 1.0f) {
            pose.scale(scaleX, scaleY, scaleZ)
        }
        pose.translate(-pivotX / 16.0f, -pivotY / 16.0f, -pivotZ / 16.0f)
        return pose
    }
}