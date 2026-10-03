package rip.ysm.compat.touhoulittlemaid.fabric.tlm.gui

import com.elfmcys.yesstevemodel.client.entity.PlayerPreviewEntity
import com.elfmcys.yesstevemodel.client.gui.button.ModelButton
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.util.ComponentUtil
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import com.github.tartaricacid.touhoulittlemaid.network.message.YsmMaidModelPackage
import net.minecraft.world.entity.Entity
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.input.InputWithModifiers
import net.minecraft.network.chat.Component
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidRenderStore

open class MaidModelButton : ModelButton {
    var maid: EntityMaid = null
    constructor(x: Int, y: Int, isAuthLocked: Boolean, previewEntity: PlayerPreviewEntity, modelAssembly: ModelAssembly, maid: EntityMaid) {
        this.maid = maid
    }
    open fun onPress(input: InputWithModifiers) {
        if (this.isStarred) {
             }
        var modelId: String = this.modelIdHolder.getModelId()
        var textureName: String = this.modelIdHolder.getCurrentTextureName()
        var displayName: Component = ComponentUtil.getDisplayName(this.renderContext, modelId)
        MaidRenderStore.getOrCreate(this.maid).setYsmModel(modelId, textureName)
        ClientPlayNetworking.send(YsmMaidModelPackage((this.maid as Entity).getId(), modelId, textureName, displayName))
    }
}