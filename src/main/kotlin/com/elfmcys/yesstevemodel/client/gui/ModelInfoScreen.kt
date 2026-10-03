package com.elfmcys.yesstevemodel.client.gui

import com.elfmcys.yesstevemodel.NameSpaces
import com.elfmcys.yesstevemodel.client.gui.button.AuthorButton
import com.elfmcys.yesstevemodel.client.gui.button.FlatColorButton
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.client.texture.OuterFileTexture
import com.elfmcys.yesstevemodel.client.upload.IResourceLocatable
import com.elfmcys.yesstevemodel.client.upload.UploadManager
import com.elfmcys.yesstevemodel.mixin.client.ScreenAccessor
import com.elfmcys.yesstevemodel.model.format.ServerModelInfo
import com.elfmcys.yesstevemodel.resource.models.AuthorInfo
import com.elfmcys.yesstevemodel.resource.models.Metadata
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.ConfirmLinkScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.util.Util
import org.apache.commons.lang3.StringUtils
import kotlin.math.max
import kotlin.math.min

class ModelInfoScreen(
    private val parentScreen: PlayerModelScreen,
    private val renderContext: ModelAssembly
) : Screen(Component.literal("Model Info GUI")) {

    private val textureList: MutableList<IResourceLocatable?> = mutableListOf()
    private val modelData: ServerModelInfo = renderContext.modelData
    private var selectedTextureIndex: Int = 0
    private var guiLeft: Int = 0
    private var guiTop: Int = 0

    init {
        initWidgets()
    }

    private fun initWidgets() {
        val textureManager: TextureManager = Minecraft.getInstance().textureManager
        textureList.clear()
        val authorInfo: List<AuthorInfo> = modelData.metadata?.authors ?: emptyList()
        val avatars: Map<String, OuterFileTexture> = renderContext.textureRegistry.authorAvatars
        for ((i, info) in authorInfo.withIndex()) {
            val avatar = avatars[info.name]
            if (avatar != null) {
                textureManager.register(NameSpaces.MOD.path("avatars/$i"), avatar)
                avatar.load()
                textureList.add(UploadManager.getOrCreateLocatable(avatar, true))
            } else {
                textureList.add(null)
            }
        }
    }

    override fun init() {
        clearWidgets()
        guiLeft = (width - 420) / 2
        guiTop = (height - 235) / 2
        val metadata: Metadata = modelData.metadata ?: return
        val authorInfos: List<AuthorInfo> = metadata.authors
        if (authorInfos.size <= selectedTextureIndex) {
            selectedTextureIndex = 0
        }

        var slot = 0
        while (slot < 5) {
            val authorIndex = selectedTextureIndex + slot
            if (authorIndex >= authorInfos.size) {
                while (slot < 5) {
                    addRenderableWidget(AuthorButton.createAuthorButton(guiLeft + 25 + (75 * slot), guiTop + 15, this))
                    slot++
                }
            } else {
                val authorInfo = authorInfos[authorIndex]
                val resourceLocatable = textureList.getOrNull(authorIndex)
                val avatarId = resourceLocatable?.getResourceLocation() ?: DEFAULT_AVATAR
                addRenderableWidget(
                    AuthorButton(
                        guiLeft + 25 + (75 * slot),
                        guiTop + 15,
                        authorInfo,
                        renderContext,
                        avatarId,
                        authorIndex,
                        this
                    )
                )
            }
            slot++
        }

        addRenderableWidget(
            FlatColorButton(guiLeft + 2, guiTop + 25, 18, 100, Component.literal("<")) {
                selectedTextureIndex = max(0, selectedTextureIndex - 5)
                init()
            }.apply { setTooltipText("gui.yes_steve_model.pre_page") }
        )

        addRenderableWidget(
            FlatColorButton(guiLeft + 25 + 375, guiTop + 25, 18, 100, Component.literal(">")) {
                selectedTextureIndex += 5
                init()
            }.apply { setTooltipText("gui.yes_steve_model.next_page") }
        )

        var linkY = guiTop + 150
        val linkCount = min(metadata.link.size, 2)
        for (linkIndex in 0 until linkCount) {
            val str = metadata.link.getKeyAt(linkIndex)
            val str2 = metadata.link.getValueAt(linkIndex)
            val component = URL_LABELS[str] ?: Component.literal(str)
            addRenderableWidget(FlatColorButton(guiLeft + 310, linkY, 85, 20, component) {
                openUrl(str2)
            })
            linkY += 25
        }

        addRenderableWidget(
            FlatColorButton(guiLeft + 310, linkY, 85, 20, Component.translatable("gui.yes_steve_model.model.return")) {
                minecraft?.setScreen(parentScreen)
            }
        )
    }

    private fun openUrl(str: String?) {
        if (!str.isNullOrBlank()) {
            minecraft?.setScreen(
                ConfirmLinkScreen({ confirmed ->
                    if (confirmed) {
                        Util.getPlatform().openUri(str)
                    }
                    minecraft?.setScreen(this)
                }, str, true)
            )
        }
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick)
        guiGraphics.fillGradient(guiLeft + 25, guiTop + 150, guiLeft + 305, guiTop + 220, -1889838245, -1889838245)
        val metadata = modelData.metadata
        if (metadata != null) {
            var lineOffset = 0
            val tips = ModelMetadataPresenter.getLocalizedModelString(renderContext, "metadata.tips", metadata.tips)
            val lines = font.split(Component.literal(tips), 270)
            for (line in lines) {
                guiGraphics.drawString(font, line, guiLeft + 30, guiTop + 154 + lineOffset, -1)
                lineOffset += 9
                if (lineOffset > 9 * 7) {
                    break
                }
            }
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        val renderables = (this as ScreenAccessor).`ysm$getRenderables`()
        renderables.forEach { renderable ->
            if (renderable is AuthorButton) {
                renderable.refreshContactComponents(guiGraphics, this, mouseX, mouseY)
            }
        }
    }

    override fun renderBlurredBackground(guiGraphics: GuiGraphics) {
    }

    override fun isPauseScreen(): Boolean = false

    companion object {
        val DEFAULT_AVATAR: Identifier = NameSpaces.MOD.path("texture/default_avatar.png")
        val URL_LABELS: Map<String, Component> = mapOf(
            "home" to Component.translatable("gui.yes_steve_model.url.home"),
            "donate" to Component.translatable("gui.yes_steve_model.url.donate")
        )
    }
}