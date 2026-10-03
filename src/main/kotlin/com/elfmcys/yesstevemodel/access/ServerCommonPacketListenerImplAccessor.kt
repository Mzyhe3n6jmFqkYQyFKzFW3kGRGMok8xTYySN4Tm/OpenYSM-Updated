package com.elfmcys.yesstevemodel.access

import net.minecraft.network.Connection

interface ServerCommonPacketListenerImplAccessor {
    fun `ysm$getConnection`(): Connection
}