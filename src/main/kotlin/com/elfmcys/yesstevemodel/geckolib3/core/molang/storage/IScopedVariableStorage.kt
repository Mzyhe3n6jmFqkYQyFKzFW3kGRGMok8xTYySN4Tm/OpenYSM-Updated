package com.elfmcys.yesstevemodel.geckolib3.core.molang.storage

interface IScopedVariableStorage {
    fun getScoped(address: Int): Any?
    fun setScoped(address: Int, value: Any)
}