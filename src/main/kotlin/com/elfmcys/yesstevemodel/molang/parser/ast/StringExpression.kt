package com.elfmcys.yesstevemodel.molang.parser.ast

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.EquipmentSlot

class StringExpression(name: String) : Expression {
    val name: String = name
    val path: Int = StringPool.computeIfAbsent(name)
    var cachedLocation: Identifier? = null
    var cachedSlot: EquipmentSlot? = null
    var slotResolved: Boolean = false

    fun getName(): String = name
    fun getPath(): Int = path

    override fun <R> visit(visitor: ExpressionVisitor<R>): R {
        return visitor.visitString(this)
    }

    override fun toString(): String = name

    fun getResourceLocation(): Identifier? = cachedLocation
    fun setResourceLocation(identifier: Identifier?) {
        cachedLocation = identifier
    }

    fun getCachedSlot(): EquipmentSlot? = cachedSlot
    fun isSlotResolved(): Boolean = slotResolved
    fun setCachedSlot(slot: EquipmentSlot?) {
        cachedSlot = slot
        slotResolved = true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (other is String) return name == other
        return other is StringExpression && path == other.path
    }

    override fun hashCode(): Int = name.hashCode()
}