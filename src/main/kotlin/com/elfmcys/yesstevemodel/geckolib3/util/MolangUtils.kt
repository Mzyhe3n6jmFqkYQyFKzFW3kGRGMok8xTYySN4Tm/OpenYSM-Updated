package com.elfmcys.yesstevemodel.geckolib3.util

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.molang.parser.ast.Expression
import com.elfmcys.yesstevemodel.molang.parser.ast.StringExpression
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.level.block.state.BlockState
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToLong

object MolangUtils {
    val SLOT_MAP: MutableMap<String, EquipmentSlot> = Object2ObjectOpenHashMap<String, EquipmentSlot>().apply {
        put("chest", EquipmentSlot.CHEST)
        put("feet", EquipmentSlot.FEET)
        put("head", EquipmentSlot.HEAD)
        put("legs", EquipmentSlot.LEGS)
        put("mainhand", EquipmentSlot.MAINHAND)
        put("offhand", EquipmentSlot.OFFHAND)
    }

    fun normalizeTime(timestamp: Long): Float = (timestamp + 6000L).toFloat() / 24000f % 1f

    fun getRelativeBlockState(
        context: ExecutionContext<IContext<Entity>>,
        args: Function.ArgumentCollection
    ): BlockState? = getRelativeBlockStateAt(context, args, 0)

    fun getRelativeBlockStateAt(
        context: ExecutionContext<IContext<Entity>>,
        args: Function.ArgumentCollection,
        i: Int
    ): BlockState? {
        val deltaX = args.getAsDouble(context, i)
        val deltaY = args.getAsDouble(context, i + 1)
        val deltaZ = args.getAsDouble(context, i + 2)
        if (abs(deltaX) > 5.0 || abs(deltaY) > 5.0 || abs(deltaZ) > 5.0) {
            return null
        }
        val entity = context.entity.entity
        val x = (entity.x + deltaX - 0.5).roundToLong().toInt()
        val y = (entity.y + deltaY - 0.5).roundToLong().toInt()
        val z = (entity.z + deltaZ - 0.5).roundToLong().toInt()
        return entity.level().getBlockState(BlockPos(x, y, z))
    }

    fun parseSlotType(context: IContext<*>, value: String?): EquipmentSlot? {
        if (value == null) {
            return null
        }
        val equipmentSlot = SLOT_MAP[value.lowercase(Locale.ENGLISH)]
        if (equipmentSlot == null) {
            context.logWarning("Illegal slot type: %s.", value)
        }
        return equipmentSlot
    }

    fun parseSlotType(
        ctx: ExecutionContext<out IContext<*>>,
        args: Function.ArgumentCollection,
        index: Int
    ): EquipmentSlot? {
        val expr: Expression = args.getExpression(index)
        if (expr is StringExpression) {
            if (expr.isSlotResolved) return expr.cachedSlot
            val name = expr.name
            val slot = SLOT_MAP[name.lowercase(Locale.ENGLISH)]
            if (slot == null) {
                ctx.entity.logWarning("Illegal slot type: %s.", name)
            }
            expr.cachedSlot = slot
            return slot
        }
        return parseSlotType(ctx.entity, args.getAsString(ctx, index))
    }
}