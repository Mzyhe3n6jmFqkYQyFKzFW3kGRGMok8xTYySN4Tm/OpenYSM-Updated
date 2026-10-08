package rip.ysm.compat.touhoulittlemaid.fabric.tlm.gui

import com.elfmcys.yesstevemodel.client.ClientModelManager
import com.elfmcys.yesstevemodel.client.entity.PlayerPreviewEntity
import com.elfmcys.yesstevemodel.client.gui.ModelInfoScreen
import com.elfmcys.yesstevemodel.client.gui.ModelMetadataPresenter
import com.elfmcys.yesstevemodel.client.gui.PlayerModelScreen
import com.elfmcys.yesstevemodel.client.gui.button.ModelButton
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.util.FileTypeUtil
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.network.chat.FormattedText
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidAnimatable
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidRenderStore

@Environment(EnvType.CLIENT)
open class MaidModelScreen(private val maid: EntityMaid) : PlayerModelScreen() {
    override fun createModelButton(
        x: Int,
        y: Int,
        isAuthLocked: Boolean,
        previewEntity: PlayerPreviewEntity,
        modelAssembly: ModelAssembly
    ): ModelButton = MaidModelButton(x, y, isAuthLocked, previewEntity, modelAssembly, maid)

    override fun createTextureScreen(
        other: PlayerModelScreen,
        str: String,
        modelAssembly: ModelAssembly
    ): Screen = MaidTextureScreen(other, str, resolveAssembly(modelAssembly), maid)

    override fun createModelInfoScreen(
        other: PlayerModelScreen,
        modelAssembly: ModelAssembly
    ): Screen = ModelInfoScreen(other, resolveAssembly(modelAssembly))

    private fun resolveAssembly(fallback: ModelAssembly): ModelAssembly {
        val current: ModelAssembly? = MaidRenderStore.getOrCreate(maid).getModelAssembly()
        return current ?: fallback
    }

    override fun renderModelPreview(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        InventoryScreen.renderEntityInInventoryFollowsMouse(
            guiGraphics,
            guiLeft + 5,
            guiTop + 29,
            guiLeft + 130,
            guiTop + 200,
            70,
            0.0625F,
            mouseX.toFloat(),
            mouseY.toFloat(),
            maid
        )

        val animatable: MaidAnimatable = MaidRenderStore.getOrCreate(maid)
        val lines = font.split(
            FormattedText.of(
                ClientModelManager.getModelContext(animatable.modelId)?.let { context ->
                    val metadata = context.modelData.metadata
                    if (metadata != null)
                        return@let ModelMetadataPresenter.getLocalizedModelString(
                            context,
                            "metadata.name",
                            metadata.name
                        )
                    StringPool.EMPTY
                }?.takeIf { it.isNotBlank() }
                    ?: FileTypeUtil.getNameWithoutArchiveExtension(animatable.modelId)
            ), 125
        )

        var lineY = guiTop + 205
        for (line in lines) {
            guiGraphics.drawString(
                font,
                line,
                guiLeft + ((135 - font.width(line)) / 2),
                lineY,
                15986656
            )
            lineY += 10
        }
    }
}
