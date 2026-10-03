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

    @JvmStatic
    fun createLocationBone(bone: AnimatedGeoBone): ILocationBone {
        return object : ILocationBone {
            override fun getRotationX(): Float = bone.getRotationX()
            override fun getRotationY(): Float = bone.getRotationY()
            override fun getRotationZ(): Float = bone.getRotationZ()

            override fun getPositionX(): Float = bone.getPositionX()
            override fun getPositionY(): Float = bone.getPositionY()
            override fun getPositionZ(): Float = bone.getPositionZ()

            override fun getScaleX(): Float = bone.getScaleX()
            override fun getScaleY(): Float = bone.getScaleY()
            override fun getScaleZ(): Float = bone.getScaleZ()

            override fun getPivotX(): Float = bone.getPivotX()
            override fun getPivotY(): Float = bone.getPivotY()
            override fun getPivotZ(): Float = bone.getPivotZ()
        }
    }

    @JvmStatic
    fun createLocationModel(model: AnimatedGeoModel): ILocationModel {
        return object : ILocationModel {
            override fun leftHandBones(): List<ILocationBone> {
                return toTlmBones(model.leftHandBones())
            }

            override fun extraLeftHandBones(): List<List<out ILocationBone>> {
                val chains = ReferenceArrayList<List<out ILocationBone>>()
                model.rightHandChain().forEach { list -> chains.add(toTlmBones(list)) }
                return chains
            }

            override fun rightHandBones(): List<ILocationBone> {
                return toTlmBones(model.rightHandBones())
            }

            override fun extraRightHandBones(): List<List<out ILocationBone>> {
                val chains = ReferenceArrayList<List<out ILocationBone>>()
                model.leftHandChains().forEach { list -> chains.add(toTlmBones(list)) }
                return chains
            }

            override fun leftWaistBones(): List<ILocationBone> {
                return toTlmBones(model.leftWaistBones())
            }

            override fun rightWaistBones(): List<ILocationBone> {
                return toTlmBones(model.rightWaistBones())
            }

            override fun backpackBones(): List<ILocationBone> {
                val backpack = model.backpackBones()
                if (backpack.isEmpty()) {
                    return toTlmBones(model.elytraBones())
                }
                return toTlmBones(backpack)
            }

            override fun tacPistolBones(): List<ILocationBone> {
                return toTlmBones(model.tacPistolBones())
            }

            override fun tacRifleBones(): List<ILocationBone> {
                return toTlmBones(model.tacRifleBones())
            }

            override fun headBones(): List<ILocationBone> {
                return toTlmBones(model.headBones())
            }
        }
    }

    private fun toTlmBones(bones: List<IBone>): List<ILocationBone> {
        return bones.map { bone -> (bone as AnimatedGeoBone).getTouhouMaidBone<ILocationBone>()!! }
    }
}
