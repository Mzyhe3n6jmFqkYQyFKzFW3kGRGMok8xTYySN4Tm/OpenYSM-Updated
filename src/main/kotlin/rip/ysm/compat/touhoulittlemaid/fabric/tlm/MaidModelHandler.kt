package rip.ysm.compat.touhoulittlemaid.fabric.tlm

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache
import com.elfmcys.yesstevemodel.model.ServerModelManager
import com.elfmcys.yesstevemodel.molang.parser.ParseException
import com.elfmcys.yesstevemodel.resource.models.ModelProperties
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import net.minecraft.world.entity.Entity
import org.apache.commons.lang3.StringUtils
import java.util.Map

class MaidModelHandler {
    constructor() {
    }
    companion object {
        @JvmStatic fun executeMaidMolang(entity: Entity, expression: String) {
            if (!entity is EntityMaid || !maid.isYsmModel()) {
                 }
            MaidRenderStore.get(maid).ifPresent({ animatable -> 

 })
        }
        @JvmStatic fun activateRouletteAnimation(entity: Entity, classify: String, index: Int) {
            if (!entity is EntityMaid || !maid.isYsmModel()) {
                 }
            if (index == -1) {
                maid.stopRouletteAnim()
                return
            }
            ServerModelManager.getModelDefinition(maid.getYsmModelId()).ifPresent({ data -> 
if (StringUtils.isNotBlank(classify) && classified.containsKey(classify)) { rouletteAnims = classified.get(classify) } else { rouletteAnims = modelProperties.getExtraAnimation() }
if (rouletteAnims.size > index) { maid.playRouletteAnim(rouletteAnims.getKeyAt(index)) }
 })
        }
    }
}