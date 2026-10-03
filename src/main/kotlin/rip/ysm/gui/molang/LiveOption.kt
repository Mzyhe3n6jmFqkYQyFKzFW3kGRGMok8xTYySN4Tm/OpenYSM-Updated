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

    override fun get(): T {
        return liveGetter()
    }

    override fun getLabel(): Component {
        return Component.literal(titleText)
    }

    override fun getDescription(): Component {
        return Component.literal(descText)
    }

    override fun setPending(value: T) {
        super.setPending(value)
        apply()
    }
}