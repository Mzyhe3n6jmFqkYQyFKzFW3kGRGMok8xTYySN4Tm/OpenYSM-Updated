package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm

import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.IValueEvaluator
import net.minecraft.world.entity.player.Player

class TextureName : IValueEvaluator<String?, IContext<Player>> {
    override fun eval(ctx: IContext<Player>): String? {
        val animatableEntity: AnimatableEntity<*> = ctx.geoInstance
        if (animatableEntity is CustomPlayerEntity) {
            return animatableEntity.currentTextureName
        }
        return null
    }
}