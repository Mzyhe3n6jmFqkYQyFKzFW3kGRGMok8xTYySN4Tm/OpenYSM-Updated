package com.elfmcys.yesstevemodel.client.model.processor

import com.elfmcys.yesstevemodel.client.entity.GeoEntity
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import java.util.function.Consumer

fun interface ControllerFactory<T : GeoEntity<*>> {
    fun create(entity: T, consumer: Consumer<IAnimationController<T>>)
}
