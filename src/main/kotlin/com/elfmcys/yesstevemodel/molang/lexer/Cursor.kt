package com.elfmcys.yesstevemodel.molang.lexer

import java.util.Objects

class Cursor(var line: Int = 0, var column: Int = 0) : Cloneable {
    var index: Int = 0
        private set

    fun index(): Int = index
    fun line(): Int = line
    fun column(): Int = column

    fun push(character: Int) {
        index++
        if (character == '\n'.code) {
            line++
            column = 1
        } else {
            column++
        }
    }

    public override fun clone(): Cursor = Cursor(line, column)

    override fun toString(): String = "line $line, column $column"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is Cursor) return false
        return line == other.line && column == other.column
    }

    override fun hashCode(): Int = Objects.hash(line, column)
}