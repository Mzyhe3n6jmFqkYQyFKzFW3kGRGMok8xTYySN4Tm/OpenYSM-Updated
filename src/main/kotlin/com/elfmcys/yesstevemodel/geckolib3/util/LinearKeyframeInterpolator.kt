package com.elfmcys.yesstevemodel.geckolib3.util

import it.unimi.dsi.fastutil.objects.ReferenceArrayList

class LinearKeyframeInterpolator : IInterpolable {
    private val segments: List<Segment>
    private val lookup: InterpolationLookup<Segment>

    //"blend_transition": {
    //	"0.0": 1,
    //	"0.01": 0.896,
    //	"0.02": 0.648,
    //	"0.03": 0.352,
    //	"0.04": 0.104,
    //	"0.05": 0
    //}
    // keys=[0.0, 0.01, 0.02, 0.03, 0.04, 0.05]
    // values=[1.0, 0.896, 0.648, 0.352, 0.104, 0.0]
    constructor(keys: FloatArray, values: FloatArray) {
        val segmentList = ReferenceArrayList<Segment>(keys.size - 1)
        for (i in 0 until keys.size - 1) {
            val startTimeTick = keys[i] * 20.0f
            val endTimeTick = keys[i + 1] * 20.0f
            val startValue = 1.0f - values[i]
            val endValue = 1.0f - values[i + 1]
            segmentList.add(Segment(startTimeTick, endTimeTick, startValue, endValue))
        }
        this.segments = segmentList
        this.lookup = InterpolationLookup(segmentList, 0.0f) { controlPoint -> controlPoint.endTime }
    }

    private constructor(list: List<Segment>) {
        this.segments = list
        this.lookup = InterpolationLookup(list, 0.0f) { controlPoint -> controlPoint.endTime }
    }

    override fun interpolate(f: Float): Float {
        val segment = lookup.getAtTime(f)
        if (f <= segment.startTime) return segment.startValue
        if (f >= segment.endTime) return segment.startValue + segment.valueDelta
        // 分段线性插值计算
        return segment.startValue + segment.valueDelta * ((f - segment.startTime) / segment.duration)
    }

    override val progress: Float
        get() = lookup.endTime

    override fun asInterpolator(): LinearKeyframeInterpolator {
        return LinearKeyframeInterpolator(segments)
    }

    class Segment(
        @JvmField val startTime: Float,
        @JvmField val endTime: Float,
        @JvmField val startValue: Float,
        endValue: Float
    ) {
        @JvmField
        val duration: Float = endTime - startTime

        @JvmField
        val valueDelta: Float = endValue - startValue
    }
}