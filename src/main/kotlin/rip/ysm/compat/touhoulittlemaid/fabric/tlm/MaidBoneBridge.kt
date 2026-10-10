package rip.ysm.compat.touhoulittlemaid.fabric.tlm

import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoBone
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.processor.ILocationBone
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.animated.ILocationModel
import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment

@Environment(EnvType.CLIENT)
object MaidBoneBridge {
    fun createLocationBone(bone: AnimatedGeoBone): ILocationBone {
        return object : ILocationBone {
            override fun getRotationX(): Float = bone.rotationX
            override fun getRotationY(): Float = bone.rotationY
            override fun getRotationZ(): Float = bone.rotationZ

            override fun getPositionX(): Float = bone.positionX
            override fun getPositionY(): Float = bone.positionY
            override fun getPositionZ(): Float = bone.positionZ

            override fun getScaleX(): Float = bone.scaleX
            override fun getScaleY(): Float = bone.scaleY
            override fun getScaleZ(): Float = bone.scaleZ

            override fun getPivotX(): Float = bone.pivotX
            override fun getPivotY(): Float = bone.pivotY
            override fun getPivotZ(): Float = bone.pivotZ
        }
    }

    fun createLocationModel(model: AnimatedGeoModel): ILocationModel {
        return object : ILocationModel {
            override fun leftHandBones(): List<ILocationBone> {
                return toTlmBones(model.leftHandBones)
            }

            override fun extraLeftHandBones(): List<List<ILocationBone>> {
                val chains = ReferenceArrayList<List<ILocationBone>>()
                model.rightHandChain.forEach { list -> chains.add(toTlmBones(list)) }
                return chains
            }

            override fun rightHandBones(): List<ILocationBone> {
                return toTlmBones(model.rightHandBones)
            }

            override fun extraRightHandBones(): List<List<ILocationBone>> {
                val chains = ReferenceArrayList<List<ILocationBone>>()
                model.leftHandChains.forEach { list -> chains.add(toTlmBones(list)) }
                return chains
            }

            override fun leftWaistBones(): List<ILocationBone> {
                return toTlmBones(model.leftWaistBones)
            }

            override fun rightWaistBones(): List<ILocationBone> {
                return toTlmBones(model.rightWaistBones)
            }

            override fun backpackBones(): List<ILocationBone> {
                val backpack = model.backpackBones
                if (backpack.isEmpty()) {
                    return toTlmBones(model.elytraBones)
                }
                return toTlmBones(backpack)
            }

            override fun tacPistolBones(): List<ILocationBone> {
                return toTlmBones(model.tacPistolBones)
            }

            override fun tacRifleBones(): List<ILocationBone> {
                return toTlmBones(model.tacRifleBones)
            }

            override fun headBones(): List<ILocationBone> {
                return toTlmBones(model.headBones)
            }
        }
    }

    private fun toTlmBones(bones: List<IBone>): List<ILocationBone> {
        return bones.map { bone -> (bone as AnimatedGeoBone).getTouhouMaidBone<ILocationBone>()!! }
    }
}
