package com.elfmcys.yesstevemodel.molang.parser

import com.elfmcys.yesstevemodel.molang.lexer.Cursor
import com.elfmcys.yesstevemodel.molang.lexer.MolangLexer
import com.elfmcys.yesstevemodel.molang.parser.ast.Expression
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding
import java.io.Closeable
import java.io.Reader

interface MolangParser : Closeable {
    fun lexer(): MolangLexer
    fun cursor(): Cursor = lexer().cursor()
    fun current(): Expression
    fun next(): Expression?

    fun parseAll(): List<Expression> {
        val expressions = mutableListOf<Expression>()
        var expr = next()
        while (expr != null) {
            expressions.add(expr)
            expr = next()
        }
        return expressions
    }

    override fun close()

    companion object {
        @JvmStatic
        fun parser(molangLexer: MolangLexer, objectBinding: ObjectBinding): MolangParser {
            return MolangParserImpl(molangLexer, objectBinding)
        }

        @JvmStatic
        fun parser(reader: Reader, objectBinding: ObjectBinding): MolangParser {
            return parser(MolangLexer.lexer(reader), objectBinding)
        }

        @JvmStatic
        fun parser(str: String, objectBinding: ObjectBinding): MolangParser {
            return parser(MolangLexer.lexer(str), objectBinding)
        }

        @JvmStatic
        fun parseExpressions(reader: Reader, objectBinding: ObjectBinding): List<Expression> {
            return parser(reader, objectBinding).use { it.parseAll() }
        }

        @JvmStatic
        fun parseExpressions(str: String, objectBinding: ObjectBinding): List<Expression> {
            return parser(str, objectBinding).use { it.parseAll() }
        }
    }
}