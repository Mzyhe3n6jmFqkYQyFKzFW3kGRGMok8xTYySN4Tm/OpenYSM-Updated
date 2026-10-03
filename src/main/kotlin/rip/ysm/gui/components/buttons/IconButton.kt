package rip.ysm.gui.components.buttons

import net.minecraft.network.chat.Component

class IconButton(
    val x: Int,
    val y: Int,
    val size: Int,
    val u: Int,
    val v: Int,
    val onPress: () -> Unit,
    val tooltip: Component?
) {
    fun contains(mx: Double, my: Double): Boolean =
        mx >= x && mx < x + size && my >= y && my < y + size
}