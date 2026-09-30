package rip.ysm.compat.touhoulittlemaid.fabric

import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoBone
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel
import rip.ysm.compat.touhoulittlemaid.TouhouMaidBoneProcessor
import rip.ysm.compat.touhoulittlemaid.TouhouMaidCompat
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidBoneBridge

object TouhouMaidBoneProcessorImpl {
    @JvmStatic
    fun isLoaded(): Boolean = TouhouMaidBoneProcessor.isModLoaded

    @JvmStatic
    fun createLocationBone(bone: AnimatedGeoBone): Any? {
        if (!TouhouMaidCompat.isModLoaded) return null
        return MaidBoneBridge.createLocationBone(bone)
    }

    @JvmStatic
    fun createLocationModel(model: AnimatedGeoModel): Any? {
        if (!TouhouMaidCompat.isModLoaded) return null
        return MaidBoneBridge.createLocationModel(model)
    }
}
