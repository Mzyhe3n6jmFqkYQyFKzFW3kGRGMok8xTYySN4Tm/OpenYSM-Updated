package rip.ysm.compat.touhoulittlemaid

import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoBone
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel
import rip.ysm.compat.ModCompat
import rip.ysm.compat.touhoulittlemaid.fabric.TouhouMaidBoneProcessorImpl

object TouhouMaidBoneProcessor : ModCompat("touhou_little_maid") {
    fun createLocationBone(bone: AnimatedGeoBone): Any? {
        if (!isModLoaded) return null
        return TouhouMaidBoneProcessorImpl.createLocationBone(bone)
    }

    fun createLocationModel(model: AnimatedGeoModel): Any? {
        if (!isModLoaded) return null
        return TouhouMaidBoneProcessorImpl.createLocationModel(model)
    }
}
