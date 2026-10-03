package rip.ysm.compat.touhoulittlemaid.fabric.tlm.gui

import com.elfmcys.yesstevemodel.client.entity.PlayerPreviewEntity
import com.elfmcys.yesstevemodel.client.gui.PlayerModelScreen
import com.elfmcys.yesstevemodel.client.gui.PlayerTextureScreen
import com.elfmcys.yesstevemodel.client.gui.button.TextureButton
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.GuiGraphics
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidAnimatable
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidRenderStore

open class MaidTextureScreen : PlayerTextureScreen {
    var maid: EntityMaid = null
    constructor(modelScreen: PlayerModelScreen, modelId: String, modelAssembly: ModelAssembly, maid: EntityMaid) {
        this.maid = maid
    }
    open fun createTextureButton(x: Int, y: Int, previewEntity: PlayerPreviewEntity, textureIndex: Int): TextureButton {
        MaidTextureButton(x, y, previewEntity, this.maid, textureIndex, this.renderContext)
    }
    open fun renderTexturePreview(guiGraphics: GuiGraphics, partialTick: Float) {
        var animatable: MaidAnimatable = MaidRenderStore.getOrCreate(this.maid)
        this.modelHolder.initModelWithTexture(animatable.getModelId(), animatable.getCurrentTextureName())
        var x0: Int = this.guiLeft + 93
        var y0: Int = this.guiTop
        var x1: Int = this.guiLeft + 299
        var y1: Int = this.guiTop + 235
        var anchorX: Float = this.guiLeft + 149.5f + 40.0f + this.offsetX
        var anchorY: Float = this.guiTop + 117.5f + 80.0f + this.offsetY
        ModelPreviewRenderer.submitTexturePreview(guiGraphics, x0, y0, x1, y1, anchorX, anchorY, this.zoom, this.pitch, this.yaw, this.modelHolder, this.showGround, partialTick)
    }
}