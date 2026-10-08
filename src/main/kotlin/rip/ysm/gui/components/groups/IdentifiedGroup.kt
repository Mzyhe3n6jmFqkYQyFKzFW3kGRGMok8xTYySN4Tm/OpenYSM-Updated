package rip.ysm.gui.components.groups

import net.minecraft.network.chat.Component
import rip.ysm.gui.OptionGroup

class IdentifiedGroup(
    id: String,
    private val displayLabel: String
) : OptionGroup(id) {
    override val title: Component
        get() = Component.literal(displayLabel)
}