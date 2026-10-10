@file:Suppress("unused")

package com.elfmcys.yesstevemodel.geckolib3.core.molang

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.client.renderer.AnimationDebugOverlay
import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.PrimaryBinding
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.FloatValue
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.MolangValue
import com.elfmcys.yesstevemodel.molang.MolangEngine
import com.elfmcys.yesstevemodel.util.log.ChatLogger
import net.minecraft.network.chat.Component

open class MolangParser(map: MutableMap<String, Any>) {
    val primaryBinding: PrimaryBinding = PrimaryBinding(map)
    val engine: MolangEngine = MolangEngine.fromCustomBinding(primaryBinding)

    open fun parseExpression(molangExpression: String, isScript: Boolean): IValue {
        return runCatching {
            parseExpressionUnsafe(molangExpression, isScript)
        }.getOrElse { e ->
            if (AnimationDebugOverlay.isDebugActive) {
                Constants.LOGGER.error("Failed to parse molang expression: {}\n{}", e.message, molangExpression)
                ChatLogger.logComponent(
                    Component.translatable("error.yes_steve_model.parse_molang_exp")
                        .append(e.message ?: "")
                        .append("\n----------------------\n")
                        .append(molangExpression.replace("\r\n", "\n").replace("\r", "\n"))
                        .append("\n----------------------")
                )
            } else {
                Constants.LOGGER.debug("Failed to parse molang expression: {}\n{}", e.message, molangExpression)
            }
            FloatValue.ZERO
        }
    }

    open fun parseExpressionUnsafe(molangExpression: String, isScript: Boolean): IValue {
        val value =
            MolangValue(engine.parse(if (isScript) stripComments(molangExpression) else molangExpression), isScript)
        primaryBinding.dispose()
        return value
    }

    open fun toFloatValue(d: Double): IValue = FloatValue(d.toFloat())

    open fun reset() = primaryBinding.reset()

    companion object {
        fun stripComments(input: String): String {
            if (input.indexOf('/') < 0) return input
            val len: Int = input.length
            val resultBuilder = StringBuilder(len)
            var inBlockComment = false
            var inLineComment = false
            var inStringLiteral = false
            var i = 0
            while (i < len) {
                val currentChar: Char = input[i]
                when {
                    inStringLiteral -> {
                        if (currentChar == '\'') inStringLiteral = false
                        resultBuilder.append(currentChar)
                    }

                    else -> {
                        when {
                            inLineComment -> {
                                if (currentChar == '\r' || currentChar == '\n') {
                                    inLineComment = false
                                    resultBuilder.append('\n')
                                }
                            }

                            else -> {
                                when {
                                    inBlockComment -> {
                                        if (currentChar == '*' && i + 1 < len) {
                                            val nextChar: Char = input[i + 1]
                                            if (nextChar == '/') {
                                                inBlockComment = false
                                                i++
                                            }
                                        }
                                    }

                                    else -> {
                                        when (currentChar) {
                                            '\'' -> {
                                                inStringLiteral = true
                                                resultBuilder.append('\'')
                                            }

                                            else -> {
                                                if (currentChar == '/' && i + 1 < len) {
                                                    val nextChar: Char = input[i + 1]
                                                    if (nextChar == '/') {
                                                        inLineComment = true
                                                        i += 2
                                                        continue
                                                    }
                                                    if (nextChar == '*') {
                                                        inBlockComment = true
                                                        i += 2
                                                        continue
                                                    }
                                                }
                                                resultBuilder.append(currentChar)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                i++
            }
            return resultBuilder.toString()
        }
    }
}