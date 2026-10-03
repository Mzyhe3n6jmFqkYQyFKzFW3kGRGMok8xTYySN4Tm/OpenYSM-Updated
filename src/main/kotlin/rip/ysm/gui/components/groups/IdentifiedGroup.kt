package rip.ysm.gui.components.groups

import net.minecraft.network.chat.Component
import rip.ysm.gui.OptionGroup

class IdentifiedGroup(
    val id: String,
    val displayLabel: String
) : OptionGroup(id) {
    override fun getTitle(): Component {
        return Component.literal(displayLabel)
    }
}