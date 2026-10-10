package com.elfmcys.yesstevemodel.molang

import com.elfmcys.yesstevemodel.molang.lexer.Cursor
import com.elfmcys.yesstevemodel.molang.parser.ParseException
import com.elfmcys.yesstevemodel.molang.parser.ast.Expression
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding
import java.io.IOException
import java.io.Reader
import java.io.StringReader

interface MolangEngine {
    fun parse(reader: Reader): List<Expression>

    fun parse(str: String): List<Expression> {
        try {
            return StringReader(str).use { parse(it) }
        } catch (e: ParseException) {
            throw e
        } catch (e2: IOException) {
            throw ParseException("Failed to close string reader", e2, Cursor())
        }
    }

    companion object {
        fun fromCustomBinding(objectBinding: ObjectBinding): MolangEngine = MolangEngineImpl(objectBinding)

        fun createEmpty(): MolangEngine = MolangEngineImpl(ObjectBinding.EMPTY)
    }
}