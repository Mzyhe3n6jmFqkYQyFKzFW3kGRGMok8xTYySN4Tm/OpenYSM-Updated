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
import rip.ysm.gui.ModernModelInfoScreen
import rip.ysm.gui.OptionRow
import kotlin.math.max
import kotlin.math.min

class AuthorRow(
    private val owner: ModernModelInfoScreen,
    private val author: AuthorInfo,
    private val authorIndex: Int,
    private val avatarLocatable: IResourceLocatable?
) : OptionRow<Any?>(0, 0, 0, AVATAR_SIZE + 8, null) {

    private var hoveredContactIndex: Int = -1

    override fun renderWidget(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val hover = isHovered
        g.fill(x, y, x + width, y + height, if (hover) 0x90171717.toInt() else 0x90000000.toInt())
        val ax = x + 4
        val ay = y + 4
        val avatar = avatarLocatable?.getResourceLocation() ?: DEFAULT_AVATAR
        g.blit(RenderPipelines.GUI_TEXTURED, avatar, ax, ay, 0.0f, 0.0f, AVATAR_SIZE, AVATAR_SIZE, 64, 64, 64, 64)

        val font: Font = Minecraft.getInstance().font
        val name = ModelMetadataPresenter.getLocalizedModelString(
            owner.renderContext,
            "metadata.authors.$authorIndex.name",
            author.name
        )
        val role = ModelMetadataPresenter.getLocalizedModelString(
            owner.renderContext,
            "metadata.authors.$authorIndex.role",
            author.role
        )
        val comment = ModelMetadataPresenter.getLocalizedModelString(
            owner.renderContext,
            "metadata.authors.$authorIndex.comment",
            author.comment
        )
        val tx = ax + AVATAR_SIZE + 8
        val textRight = x + width - 6
        val maxTextW = max(40, textRight - tx)

        g.drawString(
            font,
            Component.literal(name).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
            tx,
            y + 6,
            -1,
            false
        )
        if (!role.isNullOrBlank()) {
            val nameW = font.width(name)
            g.drawString(
                font,
                Component.literal(role).withStyle(ChatFormatting.GREEN),
                tx + nameW + 8,
                y + 6,
                -1,
                false
            )
        }
        var commentY = y + 17
        if (!comment.isNullOrBlank()) {
            val lines: List<FormattedCharSequence> = font.split(Component.literal(comment), maxTextW)
            val maxLines = min(lines.size, 2)
            for (i in 0 until maxLines) {
                g.drawString(font, lines[i], tx, commentY, 0xFFCCCCCC.toInt(), false)
                commentY += 10
            }
        }
        hoveredContactIndex = -1
        val contacts: OrderedStringMap<String, String> = author.contact
        if (contacts != null && contacts.size > 0) {
            val chipY = y + height - 14
            var chipX = tx
            for (i in 0 until contacts.size) {
                val key = contacts.getKeyAt(i)
                val chipW = font.width(key) + 8
                if (chipX + chipW > textRight) {
                    break
                }
                val chipHover = mouseX >= chipX && mouseX < chipX + chipW && mouseY >= chipY && mouseY < chipY + 12
                g.fill(
                    chipX,
                    chipY,
                    chipX + chipW,
                    chipY + 12,
                    if (chipHover) 0xC0444444.toInt() else 0x80222222.toInt()
                )
                g.drawString(font, Component.literal(key).withStyle(ChatFormatting.YELLOW), chipX + 4, chipY + 2, -1, false)
                if (chipHover) {
                    hoveredContactIndex = i
                }
                chipX += chipW + 4
            }
        }
    }

    override fun renderControl(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        if (hoveredContactIndex < 0) {
            return
        }
        val contacts: OrderedStringMap<String, String> = author.contact
        if (contacts == null || hoveredContactIndex >= contacts.size) {
            return
        }
        val value = contacts.getValueAt(hoveredContactIndex)
        if (value.isNullOrBlank()) {
            return
        }
        if (value.startsWith("http://") || value.startsWith("https://")) {
            owner.openUrlWithConfirm(value)
        } else {
            Minecraft.getInstance().keyboardHandler.clipboard = value
        }
    }

    companion object {
        val DEFAULT_AVATAR: Identifier = NameSpaces.MOD.path("texture/default_avatar.png")
        const val AVATAR_SIZE: Int = 48
    }
}