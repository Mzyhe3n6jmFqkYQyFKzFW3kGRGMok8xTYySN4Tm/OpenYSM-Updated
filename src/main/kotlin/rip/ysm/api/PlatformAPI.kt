package rip.ysm.api

import rip.ysm.api.fabric.PlatformAPIImpl

object PlatformAPI {
    @JvmStatic
    fun isServer(): Boolean {
        return PlatformAPIImpl.isServer()
    }

    @JvmStatic
    fun getPlatformName(): String {
        return PlatformAPIImpl.getPlatformName()
    }
}
