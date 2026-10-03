package rip.ysm.gui.molang

import net.minecraft.network.chat.Component
import rip.ysm.gui.Option
import java.util.function.Consumer
import java.util.function.Supplier

class LiveOption<T> : Option<T>() {
    var liveGetter: Supplier<T> = null
    var titleText: String = null
    var descText: String = null
    constructor(titleText: String, descText: String, getter: Supplier<T>, setter: Consumer<T>) {
        super("", getter, setter)
        this.liveGetter = getter
        this.titleText = titleText
        this.descText = if (descText == null) "" else descText
    }
    open fun get(): T {
        return liveGetter.get()
    }
    open fun getLabel(): Component {
        return Component.literal(titleText)
    }
    open fun getDescription(): Component {
        return Component.literal(descText)
    }
    open fun setPending(value: T) {
        super.setPending(value)
        apply()
    }
}