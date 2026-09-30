package rip.ysm.compat.touhoulittlemaid

import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoBone
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel
import rip.ysm.compat.touhoulittlemaid.fabric.TouhouMaidBoneProcessorImpl

object TouhouMaidBoneProcessor {
    @JvmStatic
    fun createLocationBone(bone: AnimatedGeoBone): Any = TouhouMaidBoneProcessorImpl.createLocationBone(bone)

    @JvmStatic
    fun createLocationModel(model: AnimatedGeoModel): Any = TouhouMaidBoneProcessorImpl.createLocationModel(model)
}
