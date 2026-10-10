package rip.ysm.compat.touhoulittlemaid.fabric.tlm

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache
import com.elfmcys.yesstevemodel.model.ServerModelManager
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import net.minecraft.world.entity.Entity
import org.apache.commons.lang3.StringUtils

object MaidModelHandler {

    fun executeMaidMolang(entity: Entity, expression: String) {
        if (entity !is EntityMaid || !entity.isYsmModel) {
            return
        }
        val animatable = MaidRenderStore.get(entity) ?: return
        runCatching {
            animatable.executeExpression(GeckoLibCache.parseSimpleExpression(expression), true, false, null)
        }.onFailure { e ->
            Constants.LOGGER.error("Failed to execute molang {}", expression, e)
        }
    }

    fun activateRouletteAnimation(entity: Entity, classify: String, index: Int) {
        if (entity !is EntityMaid || !entity.isYsmModel) return
        if (index == -1) {
            entity.stopRouletteAnim()
            return
        }
        ServerModelManager[entity.ysmModelId]?.let {
            val modelProperties = it.loadedModelData.modelProperties
            val classified = modelProperties.extraAnimationClassify
            val rouletteAnims = if (StringUtils.isNotBlank(classify) && classified.containsKey(classify))
                classified[classify]!! else modelProperties.extraAnimation
            if (rouletteAnims.size > index) {
                entity.playRouletteAnim(rouletteAnims.getKeyAt(index))
            }
        }
    }
}
