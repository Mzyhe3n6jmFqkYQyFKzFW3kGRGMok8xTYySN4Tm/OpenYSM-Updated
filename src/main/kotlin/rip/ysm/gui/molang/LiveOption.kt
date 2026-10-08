package rip.ysm.gui.molang

import net.minecraft.network.chat.Component
import rip.ysm.gui.Option

internal class LiveOption<T>(
    private val titleText: String,
    descText: String?,
    private val liveGetter: () -> T,
    setter: (T) -> Unit
) : Option<T>("", liveGetter, setter) {
    private val descText: String = descText ?: ""

    override var value: T
        get() = liveGetter()
        set(value) {
            super.value = value
            apply()
        }

    override val label: Component
        get() = Component.literal(titleText)

    override val description: Component
        get() = Component.literal(descText)
}