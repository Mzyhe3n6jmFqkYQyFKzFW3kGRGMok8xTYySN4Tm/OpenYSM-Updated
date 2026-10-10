package com.elfmcys.yesstevemodel.molang.runtime

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.molang.parser.ast.Expression
import com.elfmcys.yesstevemodel.molang.parser.ast.StringExpression
import com.elfmcys.yesstevemodel.molang.runtime.binding.ValueConversions
import net.minecraft.resources.Identifier

fun interface Function {
    fun evaluate(context: ExecutionContext<*>, arguments: ArgumentCollection): Any?

    fun validateArgumentSize(size: Int): Boolean = true

    companion object {
        val EMPTY_ARGUMENT: ArgumentCollection = ArgumentCollection(emptyList())

        val NOOP: Function = Function { _, _ -> null }
    }

    class ArgumentCollection(private val arguments: List<Expression>) {
        fun size(): Int = arguments.size

        fun getAsString(ctx: ExecutionContext<*>, index: Int): String? {
            return ValueConversions.asString(ctx.evalSafe(arguments[index]))
        }

        fun getStringId(ctx: ExecutionContext<*>, index: Int): Int {
            return ValueConversions.asStringId(ctx.evalSafe(arguments[index]))
        }

        fun getAsDouble(ctx: ExecutionContext<*>, index: Int): Double {
            return ValueConversions.asDouble(ctx.evalSafe(arguments[index]))
        }

        fun getAsInt(ctx: ExecutionContext<*>, index: Int): Int {
            return ValueConversions.asInt(ctx.evalSafe(arguments[index]))
        }

        fun getAsFloat(ctx: ExecutionContext<*>, index: Int): Float {
            return ValueConversions.asFloat(ctx.evalSafe(arguments[index]))
        }

        fun getAsBoolean(ctx: ExecutionContext<*>, index: Int): Boolean {
            return ValueConversions.asBoolean(ctx.evalSafe(arguments[index]))
        }

        fun getResourceLocation(ctx: ExecutionContext<out IContext<*>>, index: Int): Identifier? {
            val obj2 = getValue(ctx, index)
            val obj: Any?
            when (obj2) {
                is StringExpression -> {
                    if (obj2.resourceLocation != null) {
                        return obj2.resourceLocation
                    }
                    val resourceLocationTryParse = Identifier.tryParse(obj2.name)
                    if (resourceLocationTryParse != null) {
                        obj2.resourceLocation = resourceLocationTryParse
                        return resourceLocationTryParse
                    }
                    obj = obj2.name
                }

                is String -> {
                    val resourceLocationTryParse = Identifier.tryParse(obj2)
                    if (resourceLocationTryParse != null) {
                        return resourceLocationTryParse
                    }
                    obj = obj2
                }

                else -> {
                    obj = obj2
                }
            }
            ctx.entity.logWarning("Illegal resource location: ", obj ?: "null")
            return null
        }

        fun getValue(ctx: ExecutionContext<*>, index: Int): Any? {
            return ctx.evalSafe(arguments[index])
        }

        fun getExpression(i: Int): Expression {
            return arguments[i]
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || other !is ArgumentCollection) return false
            return arguments == other.arguments
        }

        override fun hashCode(): Int {
            return arguments.hashCode()
        }

        override fun toString(): String {
            return arguments.toString()
        }
    }
}