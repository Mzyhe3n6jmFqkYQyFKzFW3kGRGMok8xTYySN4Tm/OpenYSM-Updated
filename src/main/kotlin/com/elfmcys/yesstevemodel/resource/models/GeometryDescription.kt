@file:Suppress("MemberVisibilityCanBePrivate")

package com.elfmcys.yesstevemodel.resource.models

data class GeometryDescription(
    val identifier: String,
    val textureWidth: Double,
    val textureHeight: Double,
    val visibleBoundsWidth: Double,
    val visibleBoundsHeight: Double,
    val visibleBoundsOffset: DoubleArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GeometryDescription

        if (textureWidth != other.textureWidth) return false
        if (textureHeight != other.textureHeight) return false
        if (visibleBoundsWidth != other.visibleBoundsWidth) return false
        if (visibleBoundsHeight != other.visibleBoundsHeight) return false
        if (identifier != other.identifier) return false
        if (!visibleBoundsOffset.contentEquals(other.visibleBoundsOffset)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = textureWidth.hashCode()
        result = 31 * result + textureHeight.hashCode()
        result = 31 * result + visibleBoundsWidth.hashCode()
        result = 31 * result + visibleBoundsHeight.hashCode()
        result = 31 * result + identifier.hashCode()
        result = 31 * result + visibleBoundsOffset.contentHashCode()
        return result
    }
}
