package rip.ysm.compat.touhoulittlemaid.fabric.tlm.gui

import com.elfmcys.yesstevemodel.client.ClientModelManager
import com.elfmcys.yesstevemodel.client.entity.PlayerPreviewEntity
import com.elfmcys.yesstevemodel.client.gui.ModelInfoScreen
import com.elfmcys.yesstevemodel.client.gui.ModelMetadataPresenter
import com.elfmcys.yesstevemodel.client.gui.PlayerModelScreen
import com.elfmcys.yesstevemodel.client.gui.button.ModelButton
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.resource.models.Metadata
import com.elfmcys.yesstevemodel.util.FileTypeUtil
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import org.apache.commons.lang3.StringUtils
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidAnimatable
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidRenderStore
import java.util.List
import java.util.Objects

open class MaidModelScreen : PlayerModelScreen {
    var maid: EntityMaid = null
    constructor(maid: EntityMaid) {
        this.maid = maid
    }
    open fun createModelButton(x: Int, y: Int, isAuthLocked: Boolean, previewEntity: PlayerPreviewEntity, modelAssembly: ModelAssembly): ModelButton {
        MaidModelButton(x, y, isAuthLocked, previewEntity, modelAssembly, this.maid)
    }
    open fun createTextureScreen(other: PlayerModelScreen, modelId: String, modelAssembly: ModelAssembly): Screen {
        MaidTextureScreen(other, modelId, resolveAssembly(modelAssembly), this.maid)
    }
    open fun createModelInfoScreen(other: PlayerModelScreen, modelAssembly: ModelAssembly): Screen {
        ModelInfoScreen(other, resolveAssembly(modelAssembly))
    }
    open fun resolveAssembly(fallback: ModelAssembly): ModelAssembly {
        var current: ModelAssembly = MaidRenderStore.getOrCreate(this.maid).getModelAssembly()
        return Objects.requireNonNullElse(current, fallback)
    }
    open fun renderModelPreview(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, this.guiLeft + 5, this.guiTop + 29, this.guiLeft + 130, this.guiTop + 200, 70, 0.0625F, mouseX, mouseY, this.maid)
        var animatable: MaidAnimatable = MaidRenderStore.getOrCreate(this.maid)
        var lines: MutableList<FormattedCharSequence> = this.font.split(FormattedText.of(ClientModelManager.getModelContext(animatable.getModelId()).map({ context -> 
if (metadata != null) { ModelMetadataPresenter.getLocalizedModelString(context, "metadata.name", metadata.getName()) }
return StringPool.EMPTY
 }).filter(StringUtils::isNoneBlank).orElse(FileTypeUtil.getNameWithoutArchiveExtension(animatable.getModelId()))), 125)
        var lineY: Int = this.guiTop + 205
        for (line in lines) {
            guiGraphics.drawString(this.font, line, this.guiLeft + 135 - this.font.width(line) / 2, lineY, 15986656)
            lineY += 10
        }
    }
}