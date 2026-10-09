package com.elfmcys.yesstevemodel.data

import net.minecraft.nbt.CompoundTag

interface NbtSave {
    fun save(): CompoundTag
}