package com.elfmcys.yesstevemodel.molang

import com.elfmcys.yesstevemodel.molang.parser.MolangParser
import com.elfmcys.yesstevemodel.molang.parser.ast.Expression
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding
import java.io.Reader

class MolangEngineImpl(val bindings: ObjectBinding) : MolangEngine {
    override fun parse(reader: Reader): List<Expression> {
        return MolangParser.parser(reader, bindings).parseAll()
    }
}