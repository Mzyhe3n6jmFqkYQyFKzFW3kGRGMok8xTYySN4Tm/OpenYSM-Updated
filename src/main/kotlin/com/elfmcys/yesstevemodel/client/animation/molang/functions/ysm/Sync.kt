package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm

import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.AbstractClientPlayerFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function.ArgumentCollection
import com.elfmcys.yesstevemodel.network.NetworkHandler
import com.elfmcys.yesstevemodel.network.message.C2SSyncAnimationExpressionPacket
import it.unimi.dsi.fastutil.floats.FloatArrayList
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.client.player.LocalPlayer

class Sync : AbstractClientPlayerFunction() {
    override fun eval(context: ExecutionContext<IContext<AbstractClientPlayer>>, arguments: ArgumentCollection): Any? {
        if (!context.entity.isClientSide) {
            return null
        }
        if (context.entity.geoInstance is PlayerCapability && NetworkHandler.isClientConnected()) {
            if (context.entity.entity is LocalPlayer) {
                NetworkHandler.sendToServer(C2SSyncAnimationExpressionPacket(collectArgs(context, arguments)))
                return null
            }
            return null
        }
        val animatableEntity: AnimatableEntity<*> = context.entity.geoInstance
        if (animatableEntity is CustomPlayerEntity) {
            animatableEntity.executeAnimationExpression(collectArgs(context, arguments))
            return null
        }
        return null
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size <= MAX_ARGS
    }

    companion object {
        const val MAX_ARGS: Int = 16

        @JvmStatic
        fun collectArgs(
            context: ExecutionContext<IContext<AbstractClientPlayer>>,
            arguments: ArgumentCollection
        ): FloatArrayList {
            val floatArrayList = FloatArrayList(arguments.size())
            for (i in 0 until arguments.size()) {
                floatArrayList.add(arguments.getAsFloat(context, i))
            }
            return floatArrayList
        }
    }
}