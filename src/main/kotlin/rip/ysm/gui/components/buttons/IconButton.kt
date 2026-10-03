package rip.ysm.gui.components.buttons

import net.minecraft.network.chat.Component

class IconButton {
    @JvmField var x: Int = 0
    @JvmField var y: Int = 0
    @JvmField var size: Int = 0
    @JvmField var u: Int = 0
    @JvmField var v: Int = 0
    @JvmField var onPress: Runnable = null
    @JvmField var tooltip: Component = null
    constructor(x: Int, y: Int, size: Int, u: Int, v: Int, onPress: Runnable, tooltip: Component) {
        this.x = x
        this.y = y
        this.size = size
        this.u = u
        this.v = v
        this.onPress = onPress
        this.tooltip = tooltip
    }
    open fun contains(mx: Double, my: Double): Boolean {
        return mx >= x && mx < x + size && my >= y && my < y + size
    }
}