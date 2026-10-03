package com.elfmcys.yesstevemodel.geckolib3.util.json

import com.elfmcys.yesstevemodel.geckolib3.core.molang.MolangParser
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.google.gson.JsonArray
import com.google.gson.JsonElement

object JsonMolangUtils {
    // 默认不合并
    @JvmStatic
    fun getExpressions(element: JsonElement?, parser: MolangParser, mergeMultilineExpr: Boolean): Array<IValue> {
        if (element == null) {
            return emptyArray()
        }
        if (element.isJsonPrimitive && element.asJsonPrimitive.isString) {
            return arrayOf(parser.parseExpression(element.asString, false))
        }
        if (!element.isJsonArray) {
            return emptyArray()
        }
        val array: JsonArray = element.asJsonArray
        if (mergeMultilineExpr) {
            val parserText = StringBuilder()
            for (i in 0 until array.size()) {
                parserText.append(array.get(i).asString)
                if (i < array.size() - 1) {
                    parserText.append("\n")
                }
            }
            return arrayOf(parser.parseExpression(parserText.toString(), false))
        } else {
            return Array(array.size()) { i ->
                val parserText = array.get(i).asString
                parser.parseExpression(parserText, false)
            }
        }
    }
}