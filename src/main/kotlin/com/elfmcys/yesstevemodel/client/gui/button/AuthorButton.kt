package com.elfmcys.yesstevemodel.client.gui.button

import com.elfmcys.yesstevemodel.client.gui.ModelMetadataPresenter
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.resource.models.AuthorInfo
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.ConfirmLinkScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.InputWithModifiers
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.resources.Identifier
import net.minecraft.util.Util

class AuthorButton(
    x: Int,
    y: Int,
    val authorInfo: AuthorInfo?,
    val modelAssembly: ModelAssembly?,
    val avatarLocation: Identifier?,
    val authorIndex: Int,
    val parentScreen: Screen
) : Button(x, y, 70, 130, Component.empty(), {}, DEFAULT_NARRATION) {

    private val componentList: MutableList<Component> = mutableListOf()
    private var selectedContactIndex: Int = -1

    init {
        if (authorInfo != null) {
            renderTooltip(false)
        }
    }

    override fun renderContents(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val font = Minecraft.getInstance().font
        if (authorInfo == null || modelAssembly == null || avatarLocation == null) {
            guiGraphics.fillGradient(x, y, x + width, y + height, -1891417534, -1891417534)
            val grayColor = ChatFormatting.GRAY.color ?: 0xAAAAAA
            guiGraphics.drawCenteredString(
                font,
                Component.literal(""),
                x + (width / 2),
                y + (height / 2),
                grayColor or 0xFF000000.toInt()
            )
            return
        }
        if (isHoveredOrFocused) {
            guiGraphics.fillGradient(x, y, x + width, y + height, -1892652116, -1892652116)
        } else {
            guiGraphics.fillGradient(x, y, x + width, y + height, -1891417534, -1891417534)
        }
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, avatarLocation, x + 3, y + 3, 0.0f, 0.0f, 64, 64, 64, 64)
        val name = ModelMetadataPresenter.getLocalizedModelString(
            modelAssembly,
            "metadata.authors.$authorIndex.name",
            authorInfo.name
        )
        val role = ModelMetadataPresenter.getLocalizedModelString(
            modelAssembly,
            "metadata.authors.$authorIndex.role",
            authorInfo.role
        )
        val comment = ModelMetadataPresenter.getLocalizedModelString(
            modelAssembly,
            "metadata.authors.$authorIndex.comment",
            authorInfo.comment
        )
        val goldColor = ChatFormatting.GOLD.color ?: 0xFFAA00
        val greenColor = ChatFormatting.GREEN.color ?: 0x55FF55
        guiGraphics.drawString(font, name, x + 2, y + 72, goldColor or 0xFF000000.toInt(), false)
        guiGraphics.drawCenteredString(font, role, x + 35, y + 82, greenColor or 0xFF000000.toInt())
        drawWrappedText(guiGraphics, Component.literal(comment), x + 3, y + 95, 64, -1)
    }

    private fun drawWrappedText(
        guiGraphics: GuiGraphics,
        formattedText: FormattedText,
        textX: Int,
        textY: Int,
        wrapWidth: Int,
        color: Int
    ) {
        val font = Minecraft.getInstance().font
        var currentY = textY
        for (formattedCharSequence in font.split(formattedText, wrapWidth)) {
            guiGraphics.drawString(font, formattedCharSequence, textX, currentY, color, false)
            currentY += 9
            if (currentY > y + height) {
                return
            }
        }
    }

    fun refreshContactComponents(guiGraphics: GuiGraphics, screen: Screen, mouseX: Int, mouseY: Int) {
        if (isHovered && componentList.isNotEmpty()) {
            guiGraphics.setComponentTooltipForNextFrame(Minecraft.getInstance().font, componentList, mouseX, mouseY)
        } else {
            if (selectedContactIndex != -1) {
                selectedContactIndex = -1
                renderTooltip(false)
            }
        }
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (scrollY > 0.0) {
            if (selectedContactIndex > 0) {
                selectedContactIndex--
                renderTooltip(false)
                return true
            }
            return true
        }
        if (scrollY < 0.0) {
            if (selectedContactIndex < componentList.size - 2) {
                selectedContactIndex++
                renderTooltip(false)
                return true
            }
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    private fun renderTooltip(copied: Boolean) {
        if (authorInfo == null) return
        componentList.clear()
        for (i in 0 until authorInfo.contact.size) {
            val component =
                Component.literal("${authorInfo.contact.getKeyAt(i)}: ${authorInfo.contact.getValueAt(i)}")
            if (i == selectedContactIndex) {
                component.append(
                    Component.literal(if (copied) " ✓" else " ◀").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)
                )
            }
            componentList.add(component)
        }
        if (componentList.isNotEmpty()) {
            componentList.add(
                Component.translatable("gui.yes_steve_model.model.info.contact.click_hint")
                    .withStyle(ChatFormatting.DARK_GRAY)
            )
        }
    }

    override fun onPress(input: InputWithModifiers) {
        if (authorInfo == null) return
        var i = selectedContactIndex
        if (i == -1) {
            i = 0
        }
        if (i < 0 || i >= authorInfo.contact.size) return
        val link = authorInfo.contact.getValueAt(i)
        if (link.startsWith("http://") || link.startsWith("https://")) {
            Minecraft.getInstance().setScreen(
                ConfirmLinkScreen({ confirmed ->
                    if (confirmed) {
                        Util.getPlatform().openUri(link)
                    }
                    Minecraft.getInstance().setScreen(parentScreen)
                }, link, true)
            )
            return
        }
        Minecraft.getInstance().keyboardHandler.clipboard = link
        if (selectedContactIndex == -1) {
            selectedContactIndex = 0
        }
        renderTooltip(true)
    }

    companion object {
        @JvmStatic
        fun createAuthorButton(x: Int, y: Int, screen: Screen): AuthorButton {
            return AuthorButton(x, y, null, null, null, -1, screen)
        }
    }
}