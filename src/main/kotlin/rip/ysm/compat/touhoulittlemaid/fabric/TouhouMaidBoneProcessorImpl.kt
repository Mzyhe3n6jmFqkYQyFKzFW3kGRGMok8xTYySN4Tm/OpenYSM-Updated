package rip.ysm.compat.touhoulittlemaid.fabric

import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoBone
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidBoneBridge

object TouhouMaidBoneProcessorImpl {
    @JvmStatic
    fun createLocationBone(bone: AnimatedGeoBone): Any? {
        return MaidBoneBridge.createLocationBone(bone)
    }

    @JvmStatic
    fun createLocationModel(model: AnimatedGeoModel): Any? {
        return MaidBoneBridge.createLocationModel(model)
    }
}
