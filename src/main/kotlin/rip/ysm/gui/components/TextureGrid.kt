package rip.ysm.gui.components

import com.elfmcys.yesstevemodel.capability.PlayerCapability
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
import java.util.ArrayList
import java.util.List

class TextureGrid : OptionRow<Any>() {
    var owner: ModernPlayerTextureScreen = null
    var textureNames: MutableList<String> = null
    var holders: Array<PlayerPreviewEntity> = null
    constructor(owner: ModernPlayerTextureScreen) {
        super(0, 0, 0, 0, null)
        this.owner = owner
        this.textureNames = ArrayList(owner.textureMap.size())
        var i = 0
        while (i < owner.textureMap.size()) {
            this.textureNames.add(owner.textureMap.getKeyAt(i))
            i++
        }
        this.holders = arrayOfNulls<PlayerPreviewEntity>(textureNames.size())
        var i = 0
        while (i < holders.length) {
            holders[i] = PlayerPreviewEntity()
            holders[i].resetModel()
            holders[i].getAnimationStateMachine().setCurrentAnimation("idle")
            holders[i].initModelWithTexture(owner.modelId, textureNames.get(i))
            i++
        }
    }
    open fun cols(): Int {
        return Math.max(1, width + TEX_GAP / TEX_BTN_W + TEX_GAP)
    }
    open fun rows(): Int {
        return textureNames.size() + cols() - 1 / cols()
    }
    open fun setWidth(w: Int) {
        super.setWidth(w)
        this.height = rows() * TEX_BTN_H + TEX_GAP - TEX_GAP
    }
    open fun collectBlurRegions(out: MutableList<IntArray>, rowScroll: Int, areaTop: Int, areaBottom: Int) {
        var c: Int = cols()
        var slotW: Int = TEX_BTN_W + TEX_GAP
        var slotH: Int = TEX_BTN_H + TEX_GAP
        var i = 0
        while (i < textureNames.size()) {
            var col: Int = i % c
            var row: Int = i / c
            var x: Int = getX() + col * slotW
            var y: Int = getY() + row * slotH - rowScroll
            var yBot: Int = y + TEX_BTN_H
            if (yBot <= areaTop || y >= areaBottom) {
                continue
            }
            var top: Int = Math.max(y, areaTop)
            var bot: Int = Math.min(yBot, areaBottom)
            out.add(intArrayOf(x, top, TEX_BTN_W, bot - top))
            i++
        }
    }
    open fun renderWidget(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        var c: Int = cols()
        var slotW: Int = TEX_BTN_W + TEX_GAP
        var slotH: Int = TEX_BTN_H + TEX_GAP
        var i = 0
        while (i < textureNames.size()) {
            var col: Int = i % c
            var row: Int = i / c
            var x: Int = getX() + col * slotW
            var y: Int = getY() + row * slotH
            renderSlot(g, x, y, i, mouseX, mouseY, partialTick)
            i++
        }
    }
    open fun renderSlot(g: GuiGraphics, x: Int, y: Int, idx: Int, mx: Int, my: Int, pt: Float) {
        var name: String = textureNames.get(idx)
        var holder: PlayerPreviewEntity = holders[idx]
        var currentTex: String = currentTextureName()
        var selected: Boolean = name.equals(currentTex)
        var hover: Boolean = mx >= x && mx < x + TEX_BTN_W && my >= y && my < y + TEX_BTN_H
        var bg: Int = if (selected) (0x90333333).toInt() else if (hover) (0x90171717).toInt() else (0x90000000).toInt()
        g.fill(x, y, x + TEX_BTN_W, y + TEX_BTN_H, bg)
        renderHolderPreview(g, x, y, holder, pt)
        var label: Component = Component.literal(ModelMetadataPresenter.getLocalizedModelString(owner.renderContext, "files.player.texture.%s".formatted(name), name))
        var textY: Int = y + TEX_BTN_H - 12
        var tw: Int = Minecraft.getInstance().font.width(label)
        g.drawString(Minecraft.getInstance().font, label, x + TEX_BTN_W - tw / 2, textY, (0xFFFFFFFF).toInt(), true)
        if (selected || hover) {
            var border: Int = if (selected) (0xFFFFFFFF).toInt() else (0xFFAAAAAA).toInt()
            g.fill(x, y, x + TEX_BTN_W, y + 1, border)
            g.fill(x, y + TEX_BTN_H - 1, x + TEX_BTN_W, y + TEX_BTN_H, border)
            g.fill(x, y, x + 1, y + TEX_BTN_H, border)
            g.fill(x + TEX_BTN_W - 1, y, x + TEX_BTN_W, y + TEX_BTN_H, border)
        }
    }
    open fun currentTextureName(): String {
        var mc: Minecraft = Minecraft.getInstance()
        if (mc.player == null) {
            return StringPool.EMPTY
        }
        return PlayerCapability.get(mc.player).map(PlayerCapability::getCurrentTextureName).orElse(StringPool.EMPTY)
    }
    open fun renderHolderPreview(g: GuiGraphics, x: Int, y: Int, holder: PlayerPreviewEntity, pt: Float) {
        var previewH: Int = TEX_BTN_H - 20
        ModelPreviewRenderer.submitLivingEntityPreview(g, x, y, x + TEX_BTN_W, y + previewH, 35, pt, holder, false, true)
    }
    open fun renderControl(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float)
    open fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        var mouseX: Double = event.x()
        var mouseY: Double = event.y()
        var c: Int = cols()
        var slotW: Int = TEX_BTN_W + TEX_GAP
        var slotH: Int = TEX_BTN_H + TEX_GAP
        var col: Int = (mouseX - getX() / slotW as Int)
        var row: Int = (mouseY - getY() / slotH as Int)
        if (col < 0 || col >= c) {
            return
        }
        var idx: Int = row * c + col
        if (idx < 0 || idx >= textureNames.size()) {
            return
        }
        var localX: Double = mouseX - getX() - col * slotW
        var localY: Double = mouseY - getY() - row * slotH
        if (localX >= TEX_BTN_W || localY >= TEX_BTN_H) {
            return
        }
        var name: String = textureNames.get(idx)
        var mc: Minecraft = Minecraft.getInstance()
        if (mc.player == null) {
            return
        }
        PlayerCapability.get(mc.player).ifPresent({ cap -> cap.setCurrentTexture(name)
NetworkHandler.sendToServer(C2SRequestSwitchModelPacket(owner.modelId, name)) })
    }
    companion object {
        @JvmField var TEX_BTN_W: Int = 54
        @JvmField var TEX_BTN_H: Int = 102
        @JvmField var TEX_GAP: Int = 4
    }
}