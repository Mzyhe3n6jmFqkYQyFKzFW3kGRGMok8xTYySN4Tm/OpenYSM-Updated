package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.IValueEvaluator
import com.elfmcys.yesstevemodel.util.CameraUtil
import net.minecraft.client.CameraType
import net.minecraft.world.entity.player.Player
import rip.ysm.compat.firstperson.FirstPersonCompat

class FirstPersonModHide : IValueEvaluator<Boolean, IContext<Player>> {
    override fun eval(ctx: IContext<Player>): Boolean {
        if (!ctx.animationEvent().isFirstPerson && FirstPersonCompat.isModLoaded && CameraUtil.getCameraType(ctx) == CameraType.FIRST_PERSON.ordinal) {
            return FirstPersonCompat.shouldHideHead()
        }
        return false
    }
}