@file:Suppress("unused")

package com.elfmcys.yesstevemodel.extensions

import com.elfmcys.yesstevemodel.Constants
import net.minecraftforge.common.ForgeConfigSpec

fun <T> ForgeConfigSpec.ConfigValue<T>.setAndSave(value: T) {
    set(value)
    runCatching { save() }.onFailure { Constants.LOGGER.error("Config ({}) save failed", path.joinToString("/")) }
}