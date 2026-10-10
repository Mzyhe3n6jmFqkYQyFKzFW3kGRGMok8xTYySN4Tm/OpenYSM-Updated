package com.elfmcys.yesstevemodel.molang.runtime.binding

fun interface ObjectBinding {
    fun getProperty(name: String): Any?

    companion object {
        val EMPTY: ObjectBinding = ObjectBinding { null }
    }
}