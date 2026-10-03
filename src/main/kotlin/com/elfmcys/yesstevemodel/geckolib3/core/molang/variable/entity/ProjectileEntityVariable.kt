package com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.entity

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.IValueEvaluator
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.LambdaVariable
import net.minecraft.world.entity.projectile.Projectile

open class ProjectileEntityVariable(evaluator: IValueEvaluator<*, IContext<Projectile>>) :
    LambdaVariable<Projectile>(evaluator) {
    override fun validateContext(context: IContext<*>): Boolean {
        return context.entity() is Projectile
    }
}