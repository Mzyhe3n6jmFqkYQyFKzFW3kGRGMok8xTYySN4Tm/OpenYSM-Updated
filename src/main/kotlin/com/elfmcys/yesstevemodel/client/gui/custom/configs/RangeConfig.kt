package com.elfmcys.yesstevemodel.client.gui.custom.configs

import com.elfmcys.yesstevemodel.client.gui.custom.AbstractConfig

class RangeConfig(
    title: String,
    description: String,
    value: String,
    val step: Double,
    val min: Double,
    val max: Double
) : AbstractConfig(TYPE, title, description, value) {
    companion object {
        const val TYPE: String = "range"
    }
}