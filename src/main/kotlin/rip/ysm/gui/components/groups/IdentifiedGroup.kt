package rip.ysm.gui.components.groups

import net.minecraft.network.chat.Component
import rip.ysm.gui.OptionGroup

class IdentifiedGroup : OptionGroup() {
    @JvmField var id: String = null
    var displayLabel: String = null
    constructor(id: String, displayLabel: String) {
        super(id)
        this.id = id
        this.displayLabel = displayLabel
    }
    open fun getTitle(): Component {
        return Component.literal(displayLabel)
    }
}