package com.elfmcys.yesstevemodel.geckolib3.geo.render.built

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool

open class GeoBone(
    val name: String,
    val isHidden: Boolean,
    val areCubesHidden: Boolean,
    val hideChildBonesToo: Boolean,
    val pivotX: Float,
    val pivotY: Float,
    val pivotZ: Float,
    val rotX: Float,
    val rotY: Float,
    val rotZ: Float,
) {
    val boneId: Int = StringPool.computeIfAbsent(name)
    val glow: Boolean = name.startsWith(GLOWING_PREFIX)

    @JvmField var partMask: Int = 0
    @JvmField var parentIdx: Int = -1
    @JvmField var parentName: String = ""

    fun cubesAreHidden(): Boolean = areCubesHidden
    fun childBonesAreHiddenToo(): Boolean = hideChildBonesToo
    fun glow(): Boolean = glow

    companion object {
        const val GLOWING_PREFIX: String = "ysmGlow"
    }
}