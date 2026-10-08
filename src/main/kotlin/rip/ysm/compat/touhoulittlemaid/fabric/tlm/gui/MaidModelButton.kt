package rip.ysm.compat.touhoulittlemaid.fabric.tlm.gui

import com.elfmcys.yesstevemodel.client.entity.PlayerPreviewEntity
import com.elfmcys.yesstevemodel.client.gui.button.ModelButton
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.util.ComponentUtil
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import com.github.tartaricacid.touhoulittlemaid.network.message.YsmMaidModelPackage
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.input.InputWithModifiers
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Entity
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidRenderStore

@Environment(EnvType.CLIENT)
open class MaidModelButton(
    x: Int,
    y: Int,
    isAuthLocked: Boolean,
    previewEntity: PlayerPreviewEntity,
    modelAssembly: ModelAssembly,
    private val maid: EntityMaid
) : ModelButton(x, y, isAuthLocked, previewEntity, modelAssembly) {

    override fun onPress(input: InputWithModifiers) {
        if (isStarred) {
            return
        }
        val modelId: String = modelIdHolder.modelId
        val textureName: String = modelIdHolder.getCurrentTextureName() ?: ""
        val displayName: Component = ComponentUtil.getDisplayName(renderContext, modelId)

        MaidRenderStore.getOrCreate(maid).setYsmModel(modelId, textureName)
        ClientPlayNetworking.send(YsmMaidModelPackage((maid as Entity).id, modelId, textureName, displayName))
    }
}
