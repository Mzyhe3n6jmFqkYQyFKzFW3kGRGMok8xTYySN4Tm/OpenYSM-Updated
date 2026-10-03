package com.elfmcys.yesstevemodel.geckolib3.core.builder

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.elfmcys.yesstevemodel.geckolib3.util.IInterpolable
import it.unimi.dsi.fastutil.ints.IntReferenceImmutablePair
import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import it.unimi.dsi.fastutil.objects.ReferenceLists
import org.apache.commons.lang3.tuple.Pair

/**
 * 控制器状态
 */
class AnimationState(
    val name: String,
    animations: Array<Pair<String, IValue>>,
    transitions: Array<Pair<String, IValue>>,
    soundEffects: Array<String>,
    onEntry: Array<IValue>,
    onExit: Array<IValue>,
    val blendTransition: IInterpolable,
    val isBlendViaShortestPath: Boolean
) {
    val hashId: Int = StringPool.computeIfAbsent(name)
    val isBuiltinEntry: Boolean = hashId == BUILTIN_ID
    val subName: String? = if (name.startsWith(ENTRY_PREFIX)) name.substring(ENTRY_PREFIX.length) else null
    val animations: List<Pair<String, IValue>> = ReferenceLists.unmodifiable(ReferenceArrayList.wrap(animations))
    val transitions: List<IntReferenceImmutablePair<IValue>> = ReferenceLists.unmodifiable(
        ReferenceArrayList.wrap(
            transitions.map { pair ->
                IntReferenceImmutablePair(StringPool.computeIfAbsent(pair.key), pair.value)
            }.toTypedArray()
        )
    )
    val soundEffects: List<String> = ReferenceLists.unmodifiable(ReferenceArrayList.wrap(soundEffects))
    val preExpressions: List<IValue> = ReferenceLists.unmodifiable(ReferenceArrayList.wrap(onEntry))
    val postExpressions: List<IValue> = ReferenceLists.unmodifiable(ReferenceArrayList.wrap(onExit))

    companion object {
        private val BUILTIN_ID = StringPool.computeIfAbsent("ysm-builtin")
        private const val ENTRY_PREFIX = "ysm-entry-"
    }
}
