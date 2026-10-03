package com.elfmcys.yesstevemodel.molang.lexer

import java.util.Objects

class Token(
    val kind: TokenKind,
    val value: String? = null,
    val start: Int = 0,
    val end: Int = 0
) {
    init {
        if (kind.hasTag(TokenKind.Tag.HAS_VALUE) && value == null) {
            throw IllegalArgumentException("A token with kind $kind must have a non-null value")
        }
    }

    fun kind(): TokenKind = kind
    fun value(): String? = value
    fun start(): Int = start
    fun end(): Int = end

    override fun toString(): String {
        return if (kind.hasTag(TokenKind.Tag.HAS_VALUE)) {
            "$kind($value)"
        } else {
            kind.toString()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is Token) return false
        return start == other.start && end == other.end && kind == other.kind && value == other.value
    }

    override fun hashCode(): Int {
        var result = kind.hashCode()
        result = 31 * result + (value?.hashCode() ?: 0)
        result = 31 * result + start
        result = 31 * result + end
        return result
    }
}