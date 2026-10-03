package rip.ysm.gui.components.groups

import net.minecraft.client.resources.language.I18n
import net.minecraft.network.chat.Component

class TextureGroup : CategoryGroup() {
    constructor() {
        super("_textures")
    }
    open fun getTitle(): Component {
        var key: String = "gui.yes_steve_model.animation.category._textures"
        if (I18n.exists(key)) {
            return Component.translatable(key)
        }
        return Component.literal("Textures")
    }
}