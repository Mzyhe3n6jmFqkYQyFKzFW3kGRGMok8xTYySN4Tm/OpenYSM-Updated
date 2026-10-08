package rip.ysm.gui.components.groups

import net.minecraft.client.resources.language.I18n
import net.minecraft.network.chat.Component

class TextureGroup : CategoryGroup("_textures") {
    override val title: Component
        get() {
            val key = "gui.yes_steve_model.animation.category._textures"
            return if (I18n.exists(key)) Component.translatable(key) else Component.literal("Textures")
        }
}