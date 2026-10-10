package com.elfmcys.yesstevemodel.molang.lexer

object Characters {
    @JvmStatic
    fun isDigit(c: Int): Boolean = Character.isDigit(c)

    @JvmStatic
    fun isValidForWordStart(c: Int): Boolean = c in 'a'.code..'z'.code || c in 'A'.code..'Z'.code || c == '_'.code

    @JvmStatic
    fun isValidForWordContinuation(c: Int): Boolean = isValidForWordStart(c) || isDigit(c)
}