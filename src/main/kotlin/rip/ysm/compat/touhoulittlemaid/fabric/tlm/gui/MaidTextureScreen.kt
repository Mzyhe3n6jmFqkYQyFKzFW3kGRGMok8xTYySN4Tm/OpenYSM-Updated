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

@Environment(EnvType.CLIENT)
open class MaidTextureScreen(
    parentScreen: PlayerModelScreen,
    modelId: String,
    renderContext: ModelAssembly,
    private val maid: EntityMaid
) : PlayerTextureScreen(parentScreen, modelId, renderContext) {

    override fun createTextureButton(
        x: Int,
        y: Int,
        previewEntity: PlayerPreviewEntity,
        textureIndex: Int
    ): TextureButton {
        return MaidTextureButton(x, y, previewEntity, maid, textureIndex, renderContext)
    }

    override fun renderTexturePreview(guiGraphics: GuiGraphics, partialTick: Float) {
        val animatable: MaidAnimatable = MaidRenderStore.getOrCreate(maid)
        modelHolder.initModelWithTexture(animatable.modelId, animatable.getModelTextureId())
        val x0 = guiLeft + 93
        val y0 = guiTop
        val x1 = guiLeft + 299
        val y1 = guiTop + 235
        val anchorX = guiLeft + 149.5f + 40.0f + offsetX
        val anchorY = guiTop + 117.5f + 80.0f + offsetY
        ModelPreviewRenderer.submitTexturePreview(
            guiGraphics,
            x0, y0, x1, y1,
            anchorX, anchorY,
            zoom,
            pitch,
            yaw,
            modelHolder,
            showGround,
            partialTick
        )
    }
}
