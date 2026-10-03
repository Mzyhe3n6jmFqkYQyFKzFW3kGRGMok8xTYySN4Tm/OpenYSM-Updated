package com.elfmcys.yesstevemodel.util.log

import net.minecraft.network.chat.Component

interface ILogger {
    fun logFormatted(str: String, vararg objArr: Any)
    fun logComponent(component: Component)
}