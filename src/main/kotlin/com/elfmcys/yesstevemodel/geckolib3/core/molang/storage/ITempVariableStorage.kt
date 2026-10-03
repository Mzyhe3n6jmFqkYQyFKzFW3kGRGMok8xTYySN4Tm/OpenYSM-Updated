package com.elfmcys.yesstevemodel.geckolib3.core.molang.storage

interface ITempVariableStorage {
    fun getElement(i: Int): Any?
    fun setElement(i: Int, obj: Any?)
}