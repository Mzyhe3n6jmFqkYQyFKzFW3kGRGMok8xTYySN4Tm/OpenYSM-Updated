package com.elfmcys.yesstevemodel.molang.parser.ast

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.EquipmentSlot

class StringExpression(val name: String) : Expression {
    val path: Int = StringPool.computeIfAbsent(name)
    private var cachedLocation: Identifier? = null
    var cachedSlot: EquipmentSlot? = null
        set(value) {
            field = value
            slotResolved = true
        }
    private var slotResolved: Boolean = false

    override fun <R> visit(visitor: ExpressionVisitor<R>): R = visitor.visitString(this)

    override fun toString(): String = name

    var resourceLocation: Identifier?
        get() = cachedLocation
        set(value) {
            cachedLocation = value
        }

    val isSlotResolved: Boolean
        get() = slotResolved

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (other is String) return name == other
        return other is StringExpression && path == other.path
    }

    override fun hashCode(): Int = name.hashCode()
}