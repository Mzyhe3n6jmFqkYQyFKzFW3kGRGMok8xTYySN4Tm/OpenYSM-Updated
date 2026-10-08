package rip.ysm.gui.components.groups

import net.minecraft.client.resources.language.I18n
import net.minecraft.network.chat.Component
import rip.ysm.gui.OptionGroup

open class CategoryGroup(private val catKey: String) : OptionGroup("animation_category.$catKey") {
    override val title: Component
        get() {
            val key = "gui.yes_steve_model.animation.category.$catKey"
            return if (I18n.exists(key)) Component.translatable(key) else Component.literal(catKey)
        }
}