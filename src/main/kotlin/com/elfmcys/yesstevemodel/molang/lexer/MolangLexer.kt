package com.elfmcys.yesstevemodel.molang.lexer

import java.io.Closeable
import java.io.Reader
import java.io.StringReader

interface MolangLexer : Closeable {
    fun cursor(): Cursor
    fun current(): Token
    fun next(): Token

    fun tokenizeAll(): List<Token> {
        val tokens = mutableListOf<Token>()
        var token = next()
        while (token.kind != TokenKind.EOF) {
            tokens.add(token)
            token = next()
        }
        return tokens
    }

    override fun close()

    companion object {
        fun lexer(reader: Reader): MolangLexer = MolangLexerImpl(reader)

        fun lexer(string: String): MolangLexer = lexer(StringReader(string))

        fun tokenizeAll(reader: Reader): List<Token> {
            return lexer(reader).use { it.tokenizeAll() }
        }

        fun tokenizeAll(string: String): List<Token> {
            return lexer(string).use { it.tokenizeAll() }
        }
    }
}