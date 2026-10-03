package com.elfmcys.yesstevemodel.molang.lexer

import java.util.EnumSet

enum class TokenKind(vararg tags: Tag) {
    EOF,
    ERROR(Tag.HAS_VALUE),
    IDENTIFIER(Tag.HAS_VALUE),
    STRING(Tag.HAS_VALUE),
    FLOAT(Tag.HAS_VALUE),
    TRUE,
    FALSE,
    BREAK,
    CONTINUE,
    RETURN,
    DOT,
    BANG,
    AMPAMP,
    BARBAR,
    LT,
    LTE,
    GT,
    GTE,
    EQ,
    EQEQ,
    BANGEQ,
    STAR,
    SLASH,
    PLUS,
    SUB,
    LPAREN,
    RPAREN,
    LBRACE,
    RBRACE,
    QUESQUES,
    QUES,
    COLON,
    ARROW,
    LBRACKET,
    RBRACKET,
    COMMA,
    SEMICOLON;

    private val tags: Set<Tag> = if (tags.isEmpty()) emptySet() else EnumSet.copyOf(tags.toList())

    fun hasTag(tag: Tag): Boolean {
        return tags.contains(tag)
    }

    enum class Tag {
        HAS_VALUE
    }
}