package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.IValueEvaluator
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.block.HorizontalDirectionalBlock

class LadderFacing : IValueEvaluator<Int, IContext<LivingEntity>> {
    override fun eval(ctx: IContext<LivingEntity>): Int {
        val lastClimbablePos = ctx.entity.lastClimbablePos
        if (lastClimbablePos.isPresent) {
            val optionalValue = ctx.entity.level().getBlockState(lastClimbablePos.get())
                .getOptionalValue(HorizontalDirectionalBlock.FACING)
            if (optionalValue.isPresent) {
                return optionalValue.get().get2DDataValue()
            }
        }
        return 0
    }
}