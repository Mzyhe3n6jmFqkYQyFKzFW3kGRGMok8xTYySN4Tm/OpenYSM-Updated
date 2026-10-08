package com.elfmcys.yesstevemodel.client.entity

import com.elfmcys.yesstevemodel.geckolib3.core.controller.BoneTransformProvider
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneTopLevelSnapshot
import com.elfmcys.yesstevemodel.geckolib3.core.util.TransitionVector3f
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap
import rip.ysm.compat.immersiveaircraft.ImmersiveAirCraftCompat
import rip.ysm.compat.simpleplanes.SimplePlanesCompat

class VehicleRotationController(
    val entity: GeckoVehicleEntity,
    private val modelId: String
) : IAnimationController<GeckoVehicleEntity> {
    private val transformProvider: ExpressionTransformProvider = ExpressionTransformProvider()
    private var boneTarget: BoneTopLevelSnapshot? = null
    var vehicleRotation: TransitionVector3f? = null
        private set

    override val name: String
        get() = modelId

    override val currentAnimation: String
        get() = "[Coded]"

    override fun init(
        list: MutableList<BoneTopLevelSnapshot>,
        object2ReferenceMap: Object2ReferenceMap<String, MutableList<IValue>>
    ) {
        boneTarget = if (list.isEmpty()) null else list[0]
    }

    override fun process(
        event: AnimationEvent<GeckoVehicleEntity>,
        evaluator: ExpressionEvaluator<AnimationContext<*>>,
        isSomething: Boolean
    ) {
        val rot = ImmersiveAirCraftCompat.getAircraftRotation(event)
            ?: SimplePlanesCompat.getSimplePlanesRotation(event)
        if (rot != null) {
            vehicleRotation = TransitionVector3f(rot).apply {
                setPercentCompleted(0.0f)
            }
        }
    }

    override fun forEachTransform(consumer: (BoneTransformProvider) -> Unit) {
        if (boneTarget != null && vehicleRotation != null) consumer(transformProvider)
    }

    override fun reset() {
        boneTarget = null
        vehicleRotation = null
    }

    private inner class ExpressionTransformProvider : BoneTransformProvider {
        override val boneTarget: BoneTopLevelSnapshot
            get() = this@VehicleRotationController.boneTarget!!

        override fun getRotation(evaluator: ExpressionEvaluator<AnimationContext<*>>): TransitionVector3f? =
            this@VehicleRotationController.vehicleRotation

        override fun getPosition(evaluator: ExpressionEvaluator<AnimationContext<*>>): TransitionVector3f? = null

        override fun getScale(evaluator: ExpressionEvaluator<AnimationContext<*>>): TransitionVector3f? = null
    }
}