package com.elfmcys.yesstevemodel.client.upload

import net.minecraft.resources.Identifier

fun interface IResourceLocatable {
    fun getResourceLocation(): Identifier?
}