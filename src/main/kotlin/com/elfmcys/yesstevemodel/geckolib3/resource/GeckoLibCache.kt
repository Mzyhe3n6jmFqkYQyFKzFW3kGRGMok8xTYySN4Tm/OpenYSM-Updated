package com.elfmcys.yesstevemodel.geckolib3.resource

import com.elfmcys.yesstevemodel.client.animation.molang.*
import com.elfmcys.yesstevemodel.geckolib3.core.molang.MolangParser
import com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.MathBinding
import com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.QueryBinding
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.elfmcys.yesstevemodel.molang.parser.ParseException
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.regex.Pattern

object GeckoLibCache {
    private val PARSER_POOL = ConcurrentLinkedQueue<MolangParser>()
    private val EXTRA_BINDING = HashMap<String, Any>()
    private val bindings = HashMap<String, Any>()
    private val ROAMING_VAR_PATTERN =
        Pattern.compile("^([;\\s]*(v|variable)\\.roaming\\.[A-Za-z0-9_]+\\s*=[^;]+[;\\s]*)+$", Pattern.CASE_INSENSITIVE)

    val molangParser: MolangParser
        get() = PARSER_POOL.poll() ?: createMolangParser()

    fun releaseParser(parser: MolangParser) {
        parser.reset()
        PARSER_POOL.add(parser)
    }

    @Throws(ParseException::class)
    fun parseSimpleExpression(molangExpression: String): IValue {
        val parser = molangParser
        return try {
            parser.parseExpressionUnsafe(molangExpression, false)
        } finally {
            releaseParser(parser)
        }
    }

    fun createMolangParser(): MolangParser {
        if (EXTRA_BINDING.isEmpty()) {
            runCatching {
                EXTRA_BINDING["ysm"] = YSMBinding
                EXTRA_BINDING["ctrl"] = CtrlBinding
                EXTRA_BINDING["tlm"] = TLMBinding
                EXTRA_BINDING["args"] = ArgsVariable
            }.onFailure { e ->
                throw RuntimeException(e)
            }
        }
        val map = HashMap<String, Any>(EXTRA_BINDING)
        map["fn"] = FnBinding()
        return MolangParser(map)
    }

    val globalBindings: MutableMap<String, Any>
        get() {
            if (bindings.isEmpty()) {
                bindings.putAll(EXTRA_BINDING)
                bindings["math"] = MathBinding
                bindings["q"] = QueryBinding
            }
            return bindings
        }

    fun isRoamingVariableAssignment(str: String): Boolean {
        return ROAMING_VAR_PATTERN.matcher(str).find()
    }
}