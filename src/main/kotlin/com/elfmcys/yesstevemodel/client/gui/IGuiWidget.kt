package com.elfmcys.yesstevemodel.client.gui

import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import net.minecraft.network.chat.Component

interface IGuiWidget {
    fun onSyncBegin() {}
    fun onSyncError() {}
    fun onModelsLoaded(map: MutableMap<String, ModelAssembly>) {}
    fun onSyncProgress(progress: Int, total: Int) {}
    fun onModelsUpdated(map: MutableMap<String, ModelAssembly>) {}
    fun onSyncComplete() {}
    fun onSyncMessage(component: Component?) {}
}