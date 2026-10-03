package com.elfmcys.yesstevemodel.geckolib3.core.molang.storage

interface IControllerVariableStorage {
    fun getControllerVariable(address: Int): Any?
    fun setControllerVariable(address: Int, value: Any?)
}