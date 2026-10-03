package com.elfmcys.yesstevemodel.util

import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.input.KeyEvent
import rip.ysm.api.client.KeyMappingFactory

object InputUtil {
    @JvmStatic
    fun isKeyPressed(event: KeyEvent, keyMapping: KeyMapping): Boolean {
        return KeyMappingFactory.isActiveAndMatches(keyMapping, event)
    }

    @JvmStatic
    fun isPlayerReady(): Boolean {
        val minecraft = Minecraft.getInstance()
        if (minecraft.overlay != null || minecraft.screen != null || !minecraft.mouseHandler.isMouseGrabbed) {
            return false
        }
        return minecraft.isWindowActive
    }
}