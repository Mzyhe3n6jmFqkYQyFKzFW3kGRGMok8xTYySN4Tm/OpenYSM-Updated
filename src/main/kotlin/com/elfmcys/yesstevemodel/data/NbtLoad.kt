package com.elfmcys.yesstevemodel.data

import net.minecraft.nbt.CompoundTag

interface NbtLoad<T> {
    fun load(tag: CompoundTag): T
}