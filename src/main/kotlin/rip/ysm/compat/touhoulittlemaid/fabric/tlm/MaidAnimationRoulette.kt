package rip.ysm.compat.touhoulittlemaid.fabric.tlm

import com.elfmcys.yesstevemodel.client.gui.AnimationRouletteScreen
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.TamableAnimal
import net.minecraft.world.phys.EntityHitResult
import java.util.*

@Environment(EnvType.CLIENT)
object MaidAnimationRoulette {

    fun canOpenRoulette(): Boolean {
        return lookedAtOwnedYsmMaid() != null
    }

    fun openRouletteScreen() {
        val maid = lookedAtOwnedYsmMaid() ?: return
        val animatable = MaidRenderStore.get(maid) ?: return
        val modelAssembly = animatable.modelAssembly
        if (modelAssembly == null || modelAssembly.modelData.modelProperties.extraAnimation.isEmpty()) {
            return
        }
        val minecraft = Minecraft.getInstance()
        when (minecraft.screen) {
            null -> {
                minecraft.setScreen(AnimationRouletteScreen(animatable.modelId, modelAssembly, animatable))
            }

            is AnimationRouletteScreen -> {
                minecraft.setScreen(null)
            }
        }
    }

    private fun lookedAtOwnedYsmMaid(): EntityMaid? {
        val minecraft = Minecraft.getInstance()
        val localPlayer = minecraft.player ?: return null
        val hitResult = minecraft.hitResult
        if (hitResult !is EntityHitResult) {
            return null
        }
        val entity = hitResult.entity
        if (entity !is EntityMaid || !entity.isYsmModel) {
            return null
        }
        val ownerRef = (entity as TamableAnimal).ownerReference
        val ownerUuid: UUID? = ownerRef?.uuid
        return if (localPlayer.uuid == ownerUuid) entity else null
    }
}
