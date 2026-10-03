package rip.ysm.gui.components.buttons

import net.minecraft.network.chat.Component

class IconButton(
    @JvmField val x: Int,
    @JvmField val y: Int,
    @JvmField val size: Int,
    @JvmField val u: Int,
    @JvmField val v: Int,
    @JvmField val onPress: Runnable,
    @JvmField val tooltip: Component?
) {
    fun contains(mx: Double, my: Double): Boolean {
        return mx >= x && mx < x + size && my >= y && my < y + size
    }
}