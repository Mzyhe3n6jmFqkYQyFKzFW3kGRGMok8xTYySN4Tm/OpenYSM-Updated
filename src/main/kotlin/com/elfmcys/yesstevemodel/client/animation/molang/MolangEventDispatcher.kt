package com.elfmcys.yesstevemodel.client.animation.molang

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import it.unimi.dsi.fastutil.floats.FloatArrayList
import it.unimi.dsi.fastutil.floats.FloatLists
import it.unimi.dsi.fastutil.objects.ObjectLists

object MolangEventDispatcher {
    const val PLAYER_INIT: String = "player_init"
    const val PLAYER_UPDATE: String = "player_update"
    const val SYNC: String = "sync"
    const val DEFER: String = "defer"

    @JvmStatic
    fun createExpression(list: List<IValue>, floatArrayList: FloatArrayList?): IValue {
        return createUpdateExpression(list, floatArrayList ?: FloatLists.emptyList())
    }

    @JvmStatic
    fun createInitExpression(list: List<IValue>): IValue {
        return createUpdateExpression(list, ObjectLists.emptyList<Any>())
    }

    @JvmStatic
    fun createUpdateExpression(list: List<IValue>, list2: List<*>): IValue {
        return IValue { evaluator: ExpressionEvaluator<*> ->
            val entity = evaluator.entity
            if (entity is IContext<*>) {
                val mutableList = (list2 as? MutableList<*>) ?: ArrayList(list2)
                for (value in list) {
                    entity.callFunction(evaluator, value, mutableList)
                }
            }
            null
        }
    }
}