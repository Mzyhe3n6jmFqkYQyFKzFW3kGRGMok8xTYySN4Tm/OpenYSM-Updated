package com.elfmcys.yesstevemodel.client.gui.custom.configs

import com.elfmcys.yesstevemodel.client.gui.custom.AbstractConfig

class CheckboxConfig(
    title: String,
    description: String,
    value: String
) : AbstractConfig(TYPE, title, description, value) {
    companion object {
        const val TYPE: String = "checkbox"
    }
}