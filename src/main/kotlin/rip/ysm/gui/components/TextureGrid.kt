package rip.ysm.gui.components

import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.client.ClientOnlyMode
import com.elfmcys.yesstevemodel.client.ClientOnlySelection
import com.elfmcys.yesstevemodel.client.entity.PlayerPreviewEntity
import com.elfmcys.yesstevemodel.client.gui.ModelMetadataPresenter
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.network.NetworkHandler
import com.elfmcys.yesstevemodel.network.message.C2SRequestSwitchModelPacket
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import rip.ysm.gui.ModernPlayerTextureScreen
import rip.ysm.gui.OptionRow
import kotlin.math.max
import kotlin.math.min

class TextureGrid(private val owner: ModernPlayerTextureScreen) : OptionRow<Any?>(0, 0, 0, 0, null) {

    private val textureNames: List<String>
    private val holders: Array<PlayerPreviewEntity>

    init {
        val names = ArrayList<String>(owner.textureMap.size)
        for (i in 0 until owner.textureMap.size) {
            names.add(owner.textureMap.getKeyAt(i))
        }
        textureNames = names
        holders = Array(names.size) { i ->
            PlayerPreviewEntity().apply {
                resetModel()
                animationStateMachine.currentAnimation = "idle"
                initModelWithTexture(owner.modelId, names[i])
            }
        }
    }

    private fun cols(): Int = max(1, (width + TEX_GAP) / (TEX_BTN_W + TEX_GAP))

    private fun rows(): Int = (textureNames.size + cols() - 1) / cols()

    override fun setWidth(w: Int) {
        super.setWidth(w)
        height = rows() * (TEX_BTN_H + TEX_GAP) - TEX_GAP
    }

    fun collectBlurRegions(out: MutableList<IntArray>, rowScroll: Int, areaTop: Int, areaBottom: Int) {
        val c = cols()
        val slotW = TEX_BTN_W + TEX_GAP
        val slotH = TEX_BTN_H + TEX_GAP
        for (i in textureNames.indices) {
            val col = i % c
            val row = i / c
            val x = x + col * slotW
            val y = y + row * slotH - rowScroll
            val yBot = y + TEX_BTN_H
            if (yBot <= areaTop || y >= areaBottom) continue
            val top = max(y, areaTop)
            val bot = min(yBot, areaBottom)
            out.add(intArrayOf(x, top, TEX_BTN_W, bot - top))
        }
    }

    override fun renderWidget(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val c = cols()
        val slotW = TEX_BTN_W + TEX_GAP
        val slotH = TEX_BTN_H + TEX_GAP
        for (i in textureNames.indices) {
            val col = i % c
            val row = i / c
            val x = x + col * slotW
            val y = y + row * slotH
            renderSlot(g, x, y, i, mouseX, mouseY, partialTick)
        }
    }

    private fun renderSlot(g: GuiGraphics, x: Int, y: Int, idx: Int, mx: Int, my: Int, pt: Float) {
        val name = textureNames[idx]
        val holder = holders[idx]
        val currentTex = currentTextureName()
        val selected = name == currentTex
        val hover = mx >= x && mx < x + TEX_BTN_W && my >= y && my < y + TEX_BTN_H
        val bg = if (selected) 0x90333333.toInt() else if (hover) 0x90171717.toInt() else 0x90000000.toInt()
        g.fill(x, y, x + TEX_BTN_W, y + TEX_BTN_H, bg)
        renderHolderPreview(g, x, y, holder, pt)
        val label = Component.literal(
            ModelMetadataPresenter.getLocalizedModelString(
                owner.renderContext,
                "files.player.texture.%s".format(name),
                name
            )
        )
        val textY = y + TEX_BTN_H - 12
        val tw = Minecraft.getInstance().font.width(label)
        g.drawString(Minecraft.getInstance().font, label, x + (TEX_BTN_W - tw) / 2, textY, -1, true)
        if (selected || hover) {
            val border = if (selected) -1 else 0xFFAAAAAA.toInt()
            g.fill(x, y, x + TEX_BTN_W, y + 1, border)
            g.fill(x, y + TEX_BTN_H - 1, x + TEX_BTN_W, y + TEX_BTN_H, border)
            g.fill(x, y, x + 1, y + TEX_BTN_H, border)
            g.fill(x + TEX_BTN_W - 1, y, x + TEX_BTN_W, y + TEX_BTN_H, border)
        }
    }

    private fun currentTextureName(): String {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return StringPool.EMPTY
        return PlayerCapability[player]?.currentTextureName ?: StringPool.EMPTY
    }

    private fun renderHolderPreview(g: GuiGraphics, x: Int, y: Int, holder: PlayerPreviewEntity, pt: Float) {
        val previewH = TEX_BTN_H - 20
        ModelPreviewRenderer.submitLivingEntityPreview(
            g,
            x,
            y,
            x + TEX_BTN_W,
            y + previewH,
            35,
            pt,
            holder,
            false,
            true
        )
    }

    override fun renderControl(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        val mouseX = event.x()
        val mouseY = event.y()
        val c = cols()
        val slotW = TEX_BTN_W + TEX_GAP
        val slotH = TEX_BTN_H + TEX_GAP
        val col = ((mouseX - x) / slotW).toInt()
        val row = ((mouseY - y) / slotH).toInt()
        if (col !in 0..<c) return
        val idx = row * c + col
        if (idx !in textureNames.indices) return
        val localX = mouseX - x - col * slotW
        val localY = mouseY - y - row * slotH
        if (localX >= TEX_BTN_W || localY >= TEX_BTN_H) return
        val name = textureNames[idx]
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        if (NetworkHandler.isClientConnected() && !ClientOnlyMode.isForced) {
            PlayerCapability[player]?.let { cap ->
                cap.currentTexture = name
                NetworkHandler.sendToServer(C2SRequestSwitchModelPacket(owner.modelId, name))
            }
        } else {
            ClientOnlySelection.save(owner.modelId, name)
            PlayerCapability[player]?.initModelWithTexture(owner.modelId, name)
        }
    }

    companion object {
        const val TEX_BTN_W: Int = 54
        const val TEX_BTN_H: Int = 102
        const val TEX_GAP: Int = 4
    }
}