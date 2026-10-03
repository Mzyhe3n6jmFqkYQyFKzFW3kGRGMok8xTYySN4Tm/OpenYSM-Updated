package com.elfmcys.yesstevemodel.client.gui

import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.client.entity.PlayerPreviewEntity
import com.elfmcys.yesstevemodel.client.gui.button.FlatColorButton
import com.elfmcys.yesstevemodel.client.gui.button.IconButton
import com.elfmcys.yesstevemodel.client.gui.button.TextureButton
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.renderer.texture.AbstractTexture
import net.minecraft.client.resources.language.I18n
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.Mth

open class PlayerTextureScreen(
    private val parentScreen: PlayerModelScreen,
    private val modelId: String,
    val renderContext: ModelAssembly
) : Screen(Component.literal("Player Texture GUI")) {

    val modelHolder: PlayerPreviewEntity = PlayerPreviewEntity()
    private val textureMap: OrderedStringMap<String, out AbstractTexture> = renderContext.animationBundle.textures
    private val animationKeys: MutableList<String>
    private var currentAnimation: String = StringPool.EMPTY
    private var textureMaxPage: Int = 0
    private var textureCurrentPage: Int = 0
    private var animationMaxPage: Int = 0
    private var animationCurrentPage: Int = 0

    var guiLeft: Int = 0
    var guiTop: Int = 0
    var offsetX: Float = 0.0f
    var offsetY: Float = -60.0f
    var zoom: Float = 80.0f
    var yaw: Float = 165.0f
    var pitch: Float = -5.0f
    var showGround: Boolean = true

    init {
        for (holder in texturePreviewHolders) {
            holder.resetModel()
            holder.getAnimationStateMachine().setCurrentAnimation("idle")
        }
        animationKeys = ArrayList(renderContext.animationBundle.mainAnimations.keys).apply {
            removeIf { it.startsWith(HIDDEN_PREFIX) }
            sort()
        }
    }

    open fun createTextureButton(x: Int, y: Int, previewEntity: PlayerPreviewEntity, textureIndex: Int): TextureButton {
        return TextureButton(x, y, previewEntity, renderContext)
    }

    override fun init() {
        clearWidgets()
        guiLeft = (width - 420) / 2
        guiTop = (height - 235) / 2
        textureMaxPage = (textureMap.size - 1) / 4
        animationMaxPage = (animationKeys.size - 1) / 11
        if (textureCurrentPage > textureMaxPage) {
            textureCurrentPage = 0
        }
        if (animationCurrentPage > animationMaxPage) {
            animationCurrentPage = 0
        }

        addRenderableWidget(
            FlatColorButton(guiLeft + 5, guiTop, 80, 18, Component.translatable("gui.yes_steve_model.model.return")) {
                minecraft.setScreen(parentScreen)
            }
        )

        addRenderableWidget(
            IconButton(guiLeft + 281, guiTop + 2, 16, 16, 64, 16) {
                currentAnimation = "idle"
            }.apply { setTooltipText("gui.yes_steve_model.model.stop") }
        )

        addRenderableWidget(
            IconButton(guiLeft + 263, guiTop + 2, 16, 16, 48, 16) {
                offsetX = 0.0f
                offsetY = -60.0f
                zoom = 80.0f
                yaw = 165.0f
                pitch = -5.0f
            }.apply { setTooltipText("gui.yes_steve_model.model.reset") }
        )

        addRenderableWidget(
            IconButton(guiLeft + 245, guiTop + 2, 16, 16, 64, 0) {
                showGround = !showGround
            }.apply { setTooltipText("gui.yes_steve_model.model.ground") }
        )

        addRenderableWidget(
            FlatColorButton(guiLeft + 321, guiTop + 213, 18, 18, Component.literal("<")) {
                if (textureCurrentPage > 0) {
                    textureCurrentPage--
                    init()
                }
            }
        )

        addRenderableWidget(
            FlatColorButton(guiLeft + 383, guiTop + 213, 18, 18, Component.literal(">")) {
                if (textureCurrentPage < textureMaxPage) {
                    textureCurrentPage++
                    init()
                }
            }
        )

        addRenderableWidget(
            FlatColorButton(guiLeft + 11, guiTop + 214, 16, 16, Component.literal("<")) {
                if (animationCurrentPage > 0) {
                    animationCurrentPage--
                    init()
                }
            }
        )

        addRenderableWidget(
            FlatColorButton(guiLeft + 63, guiTop + 214, 16, 16, Component.literal(">")) {
                if (animationCurrentPage < animationMaxPage) {
                    animationCurrentPage++
                    init()
                }
            }
        )

        for (animSlot in 0 until 11) {
            val animIndex = animSlot + (animationCurrentPage * 11)
            if (animIndex >= animationKeys.size) break
            val animKey = animationKeys[animIndex]
            val animButtonY = guiTop + 27 + (17 * animSlot)
            val btnKey = "gui.yes_steve_model.texture.button.${animKey.replace(":", ".")}"
            val descKey = "gui.yes_steve_model.texture.button.${animKey.replace(":", ".")}.desc"
            val label: MutableComponent = if (I18n.exists(btnKey)) {
                Component.translatable(btnKey)
            } else {
                Component.literal(animKey)
            }
            val colorButton = FlatColorButton(guiLeft + 5, animButtonY, 80, 16, label) {
                currentAnimation = animKey
            }
            if (I18n.exists(descKey)) {
                colorButton.setTooltipLines(
                    mutableListOf(
                        Component.translatable(descKey).withStyle(ChatFormatting.GOLD),
                        Component.translatable("gui.yes_steve_model.texture.button.animation_name", animKey)
                            .withStyle(ChatFormatting.GRAY)
                    )
                )
            }
            addRenderableWidget(colorButton)
        }

        for (texSlot in 0 until 4) {
            val texIndex = texSlot + (textureCurrentPage * 4)
            if (texIndex >= textureMap.size) break
            val texButtonX = guiLeft + 306 + (56 * (texSlot % 2))
            val texButtonY = guiTop + 5 + (104 * (texSlot / 2))
            val previewEntity = texturePreviewHolders[texSlot]
            previewEntity.initModelWithTexture(modelId, textureMap.getKeyAt(texIndex))
            addRenderableWidget(createTextureButton(texButtonX, texButtonY, previewEntity, texIndex))
        }
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (minecraft.player == null) return
        renderBackground(guiGraphics, mouseX, mouseY, partialTick)
        guiGraphics.fillGradient(guiLeft, guiTop + 22, guiLeft + 90, guiTop + 235, -14540254, -14540254)
        guiGraphics.fillGradient(guiLeft + 93, guiTop, guiLeft + 299, guiTop + 235, -14540254, -14540254)
        guiGraphics.fillGradient(guiLeft + 302, guiTop, guiLeft + 420, guiTop + 235, -14540254, -14540254)

        if (!modelHolder.getAnimationStateMachine().isCurrentAnimation(currentAnimation)) {
            modelHolder.getAnimationStateMachine().setCurrentAnimation(currentAnimation)
        }
        renderTexturePreview(guiGraphics, minecraft.deltaTracker.getGameTimeDeltaPartialTick(false) ?: partialTick)

        val texPageStr = "${textureCurrentPage + 1}/${textureMaxPage + 1}"
        val texPageX = guiLeft + 302 + ((118 - font.width(texPageStr)) / 2)
        val texPageY = guiTop + 223
        guiGraphics.drawString(font, texPageStr, texPageX, texPageY - (9 / 2), 0xFFF3F0E0.toInt())

        val animPageStr = "${animationCurrentPage + 1}/${animationMaxPage + 1}"
        val animPageX = guiLeft + 5 + ((80 - font.width(animPageStr)) / 2)
        val animPageY = guiTop + 218
        guiGraphics.drawString(font, animPageStr, animPageX, animPageY, 0xFFF3F0E0.toInt())

        super.render(guiGraphics, mouseX, mouseY, partialTick)

        renderables.forEach { renderable ->
            if (renderable is FlatColorButton) {
                renderable.renderTooltip(guiGraphics, this, mouseX, mouseY)
            }
        }
    }

    override fun renderBlurredBackground(guiGraphics: GuiGraphics) {
    }

    open fun renderTexturePreview(guiGraphics: GuiGraphics, partialTick: Float) {
        val player = minecraft.player ?: return
        val cap = PlayerCapability[player] ?: return
        modelHolder.initModelWithTexture(modelId, cap.getCurrentTextureName())
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

    override fun mouseDragged(event: MouseButtonEvent, dragX: Double, dragY: Double): Boolean {
        val mouseX = event.x()
        val mouseY = event.y()
        val button = event.button()
        if (minecraft == null || !isInPreviewArea(mouseX, mouseY)) {
            return false
        }
        if (button == LEFT_MOUSE_BUTTON) {
            yaw += (1.5 * dragX).toFloat()
            adjustPitch(dragY.toFloat())
        }
        if (button == RIGHT_MOUSE_BUTTON) {
            offsetX += dragX.toFloat()
            offsetY += dragY.toFloat()
            return true
        }
        return true
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (minecraft == null) return false
        if (scrollY != 0.0) {
            if (isInPreviewArea(mouseX, mouseY)) {
                adjustZoom((scrollY.toFloat()) * 0.07f)
                return true
            }
            if (isInAnimationArea(mouseX, mouseY)) {
                return scrollAnimationPage(scrollY)
            }
            if (isInTextureArea(mouseX, mouseY)) {
                return scrollTexturePage(scrollY)
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    private fun scrollTexturePage(delta: Double): Boolean {
        if (delta > 0.0 && textureCurrentPage > 0) {
            textureCurrentPage--
            Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f))
            init()
            return true
        }
        if (delta < 0.0 && textureCurrentPage < textureMaxPage) {
            textureCurrentPage++
            Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f))
            init()
            return true
        }
        return true
    }

    private fun scrollAnimationPage(delta: Double): Boolean {
        if (delta > 0.0 && animationCurrentPage > 0) {
            animationCurrentPage--
            Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f))
            init()
            return true
        }
        if (delta < 0.0 && animationCurrentPage < animationMaxPage) {
            animationCurrentPage++
            Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f))
            init()
            return true
        }
        return true
    }

    private fun isInPreviewArea(mouseX: Double, mouseY: Double): Boolean {
        return mouseX > (guiLeft + 93) && mouseX < (guiLeft + 299) && mouseY > guiTop && mouseY < (guiTop + 235)
    }

    private fun isInAnimationArea(mouseX: Double, mouseY: Double): Boolean {
        return mouseX > guiLeft && mouseX < (guiLeft + 90) && mouseY > (guiTop + 22) && mouseY < (guiTop + 235)
    }

    private fun isInTextureArea(mouseX: Double, mouseY: Double): Boolean {
        return mouseX > (guiLeft + 302) && mouseX < (guiLeft + 420) && mouseY > guiTop && mouseY < (guiTop + 235)
    }

    private fun adjustPitch(deltaY: Float) {
        pitch = when {
            pitch - deltaY > MAX_PITCH -> MAX_PITCH
            pitch - deltaY < MIN_PITCH -> MIN_PITCH
            else -> pitch - deltaY
        }
    }

    private fun adjustZoom(zoomDelta: Float) {
        zoom = Mth.clamp(zoom + (zoomDelta * zoom), MIN_ZOOM, MAX_ZOOM)
    }

    override fun isPauseScreen(): Boolean = false

    companion object {
        const val HIDDEN_PREFIX: String = "——"
        const val MAX_ZOOM: Float = 360.0f
        const val MIN_ZOOM: Float = 18.0f
        const val MAX_PITCH: Float = 90.0f
        const val MIN_PITCH: Float = -90.0f
        val texturePreviewHolders: Array<PlayerPreviewEntity> = Array(4) { PlayerPreviewEntity() }
        const val LEFT_MOUSE_BUTTON: Int = 0
        const val RIGHT_MOUSE_BUTTON: Int = 1
    }
}