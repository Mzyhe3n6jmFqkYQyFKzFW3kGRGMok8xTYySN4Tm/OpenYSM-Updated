package com.elfmcys.yesstevemodel.geckolib3.geo.exception

import net.minecraft.resources.Identifier
import java.io.Serial

open class GeckoLibException @JvmOverloads constructor(
    fileLocation: Identifier,
    message: String,
    cause: Throwable? = null
) : RuntimeException("$fileLocation: $message", cause) {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 1L
    }
}