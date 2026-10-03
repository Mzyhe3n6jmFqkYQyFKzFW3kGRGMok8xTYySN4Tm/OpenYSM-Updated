package rip.ysm.gui.components.groups

import rip.ysm.gui.OptionGroup

class InfoGroup : OptionGroup() {
    constructor(key: String) {
        super("model_info." + key)
    }
}