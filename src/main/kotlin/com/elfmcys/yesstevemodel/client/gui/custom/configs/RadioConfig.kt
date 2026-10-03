package com.elfmcys.yesstevemodel.client.gui.custom.configs

import com.elfmcys.yesstevemodel.client.gui.custom.AbstractConfig
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap

class RadioConfig(
    title: String,
    description: String,
    value: String,
    val labels: OrderedStringMap<String, String>
) : AbstractConfig(TYPE, title, description, value) {
    companion object {
        const val TYPE: String = "radio"
    }
}