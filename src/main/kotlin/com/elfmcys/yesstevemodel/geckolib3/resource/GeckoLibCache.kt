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

    @JvmStatic
    fun getMolangParser(): MolangParser = PARSER_POOL.poll() ?: createMolangParser()

    @JvmStatic
    fun releaseParser(parser: MolangParser) {
        parser.reset()
        PARSER_POOL.add(parser)
    }

    @JvmStatic
    @Throws(ParseException::class)
    fun parseSimpleExpression(molangExpression: String): IValue {
        val parser = getMolangParser()
        return try {
            parser.parseExpressionUnsafe(molangExpression, false)
        } finally {
            releaseParser(parser)
        }
    }

    @JvmStatic
    fun createMolangParser(): MolangParser {
        if (EXTRA_BINDING.isEmpty()) {
            runCatching {
                EXTRA_BINDING["ysm"] = YSMBinding.INSTANCE.get()
                EXTRA_BINDING["ctrl"] = CtrlBinding.INSTANCE.get()
                EXTRA_BINDING["tlm"] = TLMBinding.INSTANCE.get()
                EXTRA_BINDING["args"] = ArgsVariable.INSTANCE
            }.onFailure { e ->
                throw RuntimeException(e)
            }
        }
        val map = HashMap<String, Any>(EXTRA_BINDING)
        map["fn"] = FnBinding()
        return MolangParser(map)
    }

    @JvmStatic
    fun getGlobalBindings(): MutableMap<String, Any> {
        if (bindings.isEmpty()) {
            bindings.putAll(EXTRA_BINDING)
            bindings["math"] = MathBinding.INSTANCE
            bindings["q"] = QueryBinding.INSTANCE
        }
        return bindings
    }

    @JvmStatic
    fun isRoamingVariableAssignment(str: String): Boolean {
        return ROAMING_VAR_PATTERN.matcher(str).find()
    }
}