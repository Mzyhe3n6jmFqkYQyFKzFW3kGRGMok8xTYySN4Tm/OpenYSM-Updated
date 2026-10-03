package rip.ysm.compat.touhoulittlemaid.fabric.tlm

import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoBone
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.processor.ILocationBone
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.animated.ILocationModel
import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import java.util.List

class MaidBoneBridge {
    constructor() {
    }
    companion object {
        @JvmStatic fun createLocationBone(bone: AnimatedGeoBone): ILocationBone {
            object : ILocationBone() { }
        }
        @JvmStatic fun createLocationModel(model: AnimatedGeoModel): ILocationModel {
            object : ILocationModel() { }
        }
        @JvmStatic fun toTlmBones(bones: MutableList<IBone>): MutableList<ILocationBone> {
            bones.stream().map({ bone -> 
((bone as AnimatedGeoBone).getTouhouMaidBone() as ILocationBone)
 }).toList()
        }
    }
}