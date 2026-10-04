package com.elfmcys.yesstevemodel.client.animation.molang

import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import java.text.DecimalFormat

class MolangWatchRegistry {
    val entries: ReferenceArrayList<WatchEntry> = ReferenceArrayList()

    fun addWatch(phase: EvaluationPhase, str: String, value: IValue) {
        entries.add(WatchEntry(str, value, phase))
    }

    fun removeWatch(str: String) {
        entries.removeIf { entry -> entry.label == str }
    }

    fun clearAll() {
        entries.clear()
    }

    fun evaluatePreAnimation(evaluator: ExpressionEvaluator<*>) {
        for (entry in entries) {
            if (entry.phase == EvaluationPhase.PRE_ANIMATION) {
                entry.evaluate(evaluator)
            }
        }
    }

    fun evaluatePostAnimation(evaluator: ExpressionEvaluator<*>) {
        for (entry in entries) {
            if (entry.phase == EvaluationPhase.POST_ANIMATION) {
                entry.evaluate(evaluator)
            }
        }
    }

    fun forEachEntry(func: (String, String) -> Unit) {
        for (entry in entries) {
            func(entry.label, entry.resultValue)
        }
    }

    enum class EvaluationPhase {
        PRE_ANIMATION,
        POST_ANIMATION
    }

    class WatchEntry(
        val label: String,
        val value: IValue,
        val phase: EvaluationPhase
    ) {
        var resultValue: String = ""

        fun evaluate(evaluator: ExpressionEvaluator<*>) {
            resultValue = runCatching {
                when (val obj = value.evalUnsafe(evaluator)) {
                    null -> "null"
                    is Number -> DECIMAL_FORMAT.format(obj)
                    else -> obj.toString()
                }
            }.getOrElse { th ->
                "Error: ${th.message}"
            }
        }
    }

    companion object {
        @JvmField
        val DECIMAL_FORMAT: DecimalFormat = DecimalFormat("#.#####")
    }
}