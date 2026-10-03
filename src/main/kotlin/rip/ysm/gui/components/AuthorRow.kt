package rip.ysm.gui.components

import com.elfmcys.yesstevemodel.NameSpaces
import com.elfmcys.yesstevemodel.client.gui.ModelMetadataPresenter
import com.elfmcys.yesstevemodel.client.upload.IResourceLocatable
import com.elfmcys.yesstevemodel.resource.models.AuthorInfo
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.util.FormattedCharSequence
import org.apache.commons.lang3.StringUtils
import rip.ysm.gui.ModernModelInfoScreen
import rip.ysm.gui.OptionRow
import java.util.List

class AuthorRow : OptionRow<Any>() {
    var owner: ModernModelInfoScreen = null
    var author: AuthorInfo = null
    var authorIndex: Int = 0
    var avatarLocatable: IResourceLocatable = null
    var hoveredContactIndex: Int = -1
    constructor(owner: ModernModelInfoScreen, author: AuthorInfo, authorIndex: Int, avatarLocatable: IResourceLocatable) {
        super(0, 0, 0, AVATAR_SIZE + 8, null)
        this.owner = owner
        this.author = author
        this.authorIndex = authorIndex
        this.avatarLocatable = avatarLocatable
    }
    open fun renderWidget(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        var hover: Boolean = isHovered()
        g.fill(getX(), getY(), getX() + width, getY() + height, if (hover) (0x90171717).toInt() else (0x90000000).toInt())
        var ax: Int = getX() + 4
        var ay: Int = getY() + 4
        var avatar: Identifier = if (avatarLocatable != null) avatarLocatable.getResourceLocation().orElse(DEFAULT_AVATAR) else DEFAULT_AVATAR
        g.blit(RenderPipelines.GUI_TEXTURED, avatar, ax, ay, 0.0f, 0.0f, AVATAR_SIZE, AVATAR_SIZE, 64, 64, 64, 64)
        var font: Font = Minecraft.getInstance().font
        var name: String = ModelMetadataPresenter.getLocalizedModelString(owner.renderContext, "metadata.authors.%d.name".formatted(authorIndex), author.getName())
        var role: String = ModelMetadataPresenter.getLocalizedModelString(owner.renderContext, "metadata.authors.%d.role".formatted(authorIndex), author.getRole())
        var comment: String = ModelMetadataPresenter.getLocalizedModelString(owner.renderContext, "metadata.authors.%d.comment".formatted(authorIndex), author.getComment())
        var tx: Int = ax + AVATAR_SIZE + 8
        var textRight: Int = getX() + width - 6
        var maxTextW: Int = Math.max(40, textRight - tx)
        g.drawString(font, Component.literal(name).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), tx, getY() + 6, -1, false)
        if (StringUtils.isNotBlank(role)) {
            var nameW: Int = font.width(name)
            g.drawString(font, Component.literal(role).withStyle(ChatFormatting.GREEN), tx + nameW + 8, getY() + 6, -1, false)
        }
        var commentY: Int = getY() + 17
        if (StringUtils.isNotBlank(comment)) {
            var lines: MutableList<FormattedCharSequence> = font.split(Component.literal(comment), maxTextW)
            var max: Int = Math.min(lines.size(), 2)
            var i = 0
            while (i < max) {
                g.drawString(font, lines.get(i), tx, commentY, (0xFFCCCCCC).toInt(), false)
                commentY += 10
                i++
            }
        }
        hoveredContactIndex = -1
        var contacts: OrderedStringMap<String, String> = author.getContact()
        if (contacts != null && contacts.size() > 0) {
            var chipY: Int = getY() + height - 14
            var chipX: Int = tx
            var i = 0
            while (i < contacts.size()) {
                var key: String = contacts.getKeyAt(i)
                var chipW: Int = font.width(key) + 8
                if (chipX + chipW > textRight) {
                    break
                }
                var chipHover: Boolean = mouseX >= chipX && mouseX < chipX + chipW && mouseY >= chipY && mouseY < chipY + 12
                g.fill(chipX, chipY, chipX + chipW, chipY + 12, if (chipHover) (0xC0444444).toInt() else (0x80222222).toInt())
                g.drawString(font, Component.literal(key).withStyle(ChatFormatting.YELLOW), chipX + 4, chipY + 2, -1, false)
                if (chipHover) {
                    hoveredContactIndex = i
                }
                chipX += chipW + 4
                i++
            }
        }
    }
    open fun renderControl(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float)
    open fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        if (hoveredContactIndex < 0) {
            return
        }
        var contacts: OrderedStringMap<String, String> = author.getContact()
        if (contacts == null || hoveredContactIndex >= contacts.size()) {
            return
        }
        var value: String = contacts.getValueAt(hoveredContactIndex)
        if (StringUtils.isBlank(value)) {
            return
        }
        if (value.startsWith("http://") || value.startsWith("https://")) {
            owner.openUrlWithConfirm(value)
        } else {
            Minecraft.getInstance().keyboardHandler.setClipboard(value)
        }
    }
    companion object {
        @JvmField var DEFAULT_AVATAR: Identifier = NameSpaces.MOD.path("texture/default_avatar.png")
        @JvmField var AVATAR_SIZE: Int = 48
    }
}