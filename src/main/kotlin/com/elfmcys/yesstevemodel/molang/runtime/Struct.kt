package com.elfmcys.yesstevemodel.molang.runtime

interface Struct {
    fun getProperty(name: Int): Any?
    fun putProperty(name: Int, value: Any?)
    fun copy(): Struct
}