package com.elfmcys.yesstevemodel.client.model.processor

import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import com.elfmcys.yesstevemodel.client.entity.GeoEntity
import java.util.function.Consumer

interface ControllerFactory<T> {
    fun create(entity: T, consumer: Consumer<IAnimationController<T>>)
}