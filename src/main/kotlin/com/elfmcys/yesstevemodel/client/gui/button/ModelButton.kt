package com.elfmcys.yesstevemodel.client.gui.button

import com.elfmcys.yesstevemodel.NameSpaces
import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.capability.StarModelsCapability
import com.elfmcys.yesstevemodel.client.entity.PlayerPreviewEntity
import com.elfmcys.yesstevemodel.client.gui.ModelMetadataPresenter
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer
import com.elfmcys.yesstevemodel.client.upload.IResourceLocatable
import com.elfmcys.yesstevemodel.client.upload.UploadManager
import com.elfmcys.yesstevemodel.config.GeneralConfig
import com.elfmcys.yesstevemodel.network.NetworkHandler
import com.elfmcys.yesstevemodel.network.message.C2SRequestSwitchModelPacket
import com.elfmcys.yesstevemodel.util.FileTypeUtil
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.InputWithModifiers
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier
import net.minecraft.util.Util
import java.util.*

open class ModelButton(
    x: Int,
    y: Int,
    val isStarred: Boolean,
    val modelIdHolder: PlayerPreviewEntity,
    val renderContext: ModelAssembly
) : Button(x, y, 52, 90, createDisplayName(modelIdHolder, renderContext), {}, DEFAULT_NARRATION) {
    private val backgroundColor: Int = if (isStarred) 2130706432 else -12369342
    private val modelId: String
    private val modelName: String
    private val authorName: String
    private val animationDuration: Double
    private val disablePreviewRotation: Boolean = renderContext.modelData.modelProperties.disablePreviewRotation
    private val displayName: Component =
        Component.literal(FileTypeUtil.getNameWithoutArchiveExtension(modelIdHolder.getModelId()))
    private var backgroundTexture: IResourceLocatable? = renderContext.textureRegistry.getGuiBackground()?.let {
        UploadManager.getOrCreateLocatableWithSize(it, true)
    }
    private var foregroundTexture: IResourceLocatable? = renderContext.textureRegistry.getGuiForeground()?.let {
        UploadManager.getOrCreateLocatableWithSize(it, true)
    }
    private var cachedLanguage: String? = null
    private var tooltipLines: List<Component>? = null
    private var detailedTooltipLines: List<Component>? = null
    private var lastHoverTime: Long = -1L

    init {
        val animations = renderContext.animationBundle.mainAnimations
        modelId = if (animations.containsKey("hover")) "hover" else "empty"
        if (animations.containsKey("hover_fadeout")) {
            modelName = "hover_fadeout"
            animationDuration = animations["hover_fadeout"]!!.animationLength * 50.0
        } else {
            modelName = "empty"
            animationDuration = 0.0
        }
        authorName = if (animations.containsKey("focus")) "focus" else "empty"
    }

    override fun getMessage(): Component {
        if (GeneralConfig.SHOW_MODEL_ID_FIRST.get()) {
            return displayName
        }
        return super.getMessage()
    }

    override fun onPress(input: InputWithModifiers) {
        val localPlayer = Minecraft.getInstance().player
        if (!isStarred && localPlayer != null) {
            val cap = PlayerCapability[localPlayer] ?: return
            val currentTexture = modelIdHolder.getCurrentTextureName() ?: ""
            if (NetworkHandler.isClientConnected()) {
                val modelAssembly = modelIdHolder.getModelAssembly()
                if (modelAssembly != null && cap.hasMolangVars(modelAssembly.modelData.hashId)) {
                    cap.initModelWithTexture(modelIdHolder.getModelId(), currentTexture)
                    NetworkHandler.sendToServer(
                        C2SRequestSwitchModelPacket(
                            cap.getModelId(),
                            cap.getCurrentTextureName() ?: currentTexture
                        )
                    )
                    return
                } else {
                    NetworkHandler.sendToServer(C2SRequestSwitchModelPacket(modelIdHolder.getModelId(), currentTexture))
                    return
                }
            }
            cap.initModelWithTexture(modelIdHolder.getModelId(), currentTexture)
        }
    }

    override fun renderContents(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val tracker = modelIdHolder.getAnimationStateMachine()
        if (isHovered) {
            lastHoverTime = Util.getMillis()
            tracker.setPreviousAnimation(modelId)
        } else {
            if (Util.getMillis() - lastHoverTime < animationDuration) {
                tracker.setPreviousAnimation(modelName)
            } else {
                tracker.setPreviousAnimation("empty")
            }
        }
        if (isFocused) {
            tracker.setQueuedAnimation(authorName)
        } else {
            tracker.setQueuedAnimation("empty")
        }

        val minecraft = Minecraft.getInstance()
        val font = minecraft.font
        guiGraphics.fillGradient(x, y, x + width, y + height, backgroundColor, backgroundColor)

        val bg = backgroundTexture
        if (bg != null) {
            val res = bg.getResourceLocation()
            if (res != null) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, res, x, y, 0.0f, 0.0f, width, height, width, height)
            }
        }
        guiGraphics.enableScissor(x, y, x + width, (y + height) - 20)
        ModelPreviewRenderer.submitLivingEntityPreview(
            guiGraphics,
            x, y,
            x + width, y + 76,
            30,
            minecraft.deltaTracker.getGameTimeDeltaPartialTick(false),
            modelIdHolder,
            disablePreviewRotation,
            true
        )
        guiGraphics.disableScissor()

        val fg = foregroundTexture
        if (fg != null) {
            val res = fg.getResourceLocation()
            if (res != null) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, res, x, y, 0.0f, 0.0f, width, height, width, height)
            }
        }

        val listSplit = font.split(message, 45)
        if (listSplit.size > 1) {
            guiGraphics.drawCenteredString(font, listSplit[0], x + (width / 2), (y + height) - 19, 0xFFF3F0E0.toInt())
            guiGraphics.drawCenteredString(font, listSplit[1], x + (width / 2), (y + height) - 10, 0xFFF3F0E0.toInt())
        } else {
            guiGraphics.drawCenteredString(font, message, x + (width / 2), (y + height) - 15, 0xFFF3F0E0.toInt())
        }

        if (!isStarred && isHoveredOrFocused) {
            guiGraphics.fillGradient(x, y + 1, x + 1, (y + height) - 1, -790560, -790560)
            guiGraphics.fillGradient(x, y, x + width, y + 1, -790560, -790560)
            guiGraphics.fillGradient((x + width) - 1, y + 1, x + width, (y + height) - 1, -790560, -790560)
            guiGraphics.fillGradient(x, (y + height) - 1, x + width, y + height, -790560, -790560)
        }
        if (isStarred) {
            guiGraphics.fillGradient(x, y, x + width, y + height, -1625152990, -1625152990)
        }

        val player = minecraft.player
        if (player != null) {
            val starCap = StarModelsCapability[player]
            if (starCap != null && starCap.containsModel(modelIdHolder.getModelId())) {
                guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    ICON_TEXTURE,
                    (x + width) - 14,
                    y,
                    16.0f,
                    0.0f,
                    16,
                    16,
                    256,
                    256
                )
            }
        }
    }

    fun renderTooltip(guiGraphics: GuiGraphics, screen: Screen, mouseX: Int, mouseY: Int) {
        if (isHovered) {
            val selected = Minecraft.getInstance().languageManager.selected
            if (!Objects.equals(cachedLanguage, selected)) {
                cachedLanguage = selected
                detailedTooltipLines = null
                tooltipLines = null
            }
            if (InputConstants.isKeyDown(
                    Minecraft.getInstance().window,
                    340
                ) || InputConstants.isKeyDown(Minecraft.getInstance().window, 344)
            ) {
                if (detailedTooltipLines == null) {
                    detailedTooltipLines = ModelMetadataPresenter.buildModelTooltip(
                        renderContext,
                        selected,
                        modelIdHolder.getModelId(),
                        true
                    )
                }
                guiGraphics.setComponentTooltipForNextFrame(
                    Minecraft.getInstance().font,
                    detailedTooltipLines ?: return,
                    mouseX,
                    mouseY
                )
            } else {
                if (tooltipLines == null) {
                    tooltipLines = ModelMetadataPresenter.buildModelTooltip(
                        renderContext,
                        selected,
                        modelIdHolder.getModelId(),
                        false
                    )
                }
                guiGraphics.setComponentTooltipForNextFrame(
                    Minecraft.getInstance().font,
                    tooltipLines ?: return,
                    mouseX,
                    mouseY
                )
            }
        }
    }

    fun clicked(mouseX: Double, mouseY: Double): Boolean =
        !isStarred && active && visible && isMouseOver(mouseX, mouseY)

    companion object {
        val ICON_TEXTURE: Identifier = NameSpaces.MOD.path("texture/icon.png")

        @JvmStatic
        fun createDisplayName(previewEntity: PlayerPreviewEntity, modelAssembly: ModelAssembly): MutableComponent {
            val metadata = modelAssembly.modelData.metadata
            if (metadata == null || metadata.name.isBlank())
                return Component.literal(FileTypeUtil.getNameWithoutArchiveExtension(previewEntity.getModelId()))
            return Component.literal(
                ModelMetadataPresenter.getLocalizedModelString(
                    modelAssembly,
                    "metadata.name",
                    metadata.name
                )
            )
        }
    }
}