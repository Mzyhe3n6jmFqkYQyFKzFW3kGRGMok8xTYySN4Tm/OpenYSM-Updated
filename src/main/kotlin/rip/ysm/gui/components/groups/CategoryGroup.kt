package rip.ysm.gui.components.groups

import net.minecraft.client.resources.language.I18n
import net.minecraft.network.chat.Component
import rip.ysm.gui.OptionGroup

open class CategoryGroup : OptionGroup() {
    var catKey: String = null
    constructor(catKey: String) {
        super("animation_category." + catKey)
        this.catKey = catKey
    }
    open fun getTitle(): Component {
        var key: String = "gui.yes_steve_model.animation.category." + catKey
        if (I18n.exists(key)) {
            return Component.translatable(key)
        }
        return Component.literal(catKey)
    }
}