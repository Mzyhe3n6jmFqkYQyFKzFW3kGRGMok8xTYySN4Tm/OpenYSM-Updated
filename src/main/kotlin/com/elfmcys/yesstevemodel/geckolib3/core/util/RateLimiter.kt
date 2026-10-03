package com.elfmcys.yesstevemodel.geckolib3.core.util

class RateLimiter {
    var interval: Float = 0.008333334f
        private set

    private var aggregate: Float = 1.0f
    private var lastRequestTime: Float = 0.0f

    /**
     * 设置动画的目标帧率
     * @param limitPerSec 每秒更新的帧数 (例如: 24, 30, 60)
     */
    fun setRefreshRate(limitPerSec: Int) {
        interval = 1.0f / limitPerSec
    }

    fun request(time: Float): Boolean {
        aggregate += time - lastRequestTime
        lastRequestTime = time
        if (aggregate < interval) {
            return false
        }
        aggregate %= interval
        return true
    }

    fun reset() {
        aggregate = interval
        lastRequestTime = 0.0f
    }
}
