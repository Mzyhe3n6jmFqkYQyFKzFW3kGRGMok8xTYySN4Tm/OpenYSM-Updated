package com.elfmcys.yesstevemodel.molang.runtime

interface Struct {
    operator fun get(name: Int): Any?
    operator fun set(name: Int, value: Any?)
    fun copy(): Struct
}