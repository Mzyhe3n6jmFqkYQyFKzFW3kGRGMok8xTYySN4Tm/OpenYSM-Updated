package com.elfmcys.yesstevemodel.client.model.processor

import com.elfmcys.yesstevemodel.client.entity.GeoEntity
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle
import java.util.function.Predicate

interface ModelProcessor<T, TModel> {
    fun process(modelData: TModel, resourceBundle: ModelResourceBundle): ControllerFactory<T>
    fun withFilter(predicate: Predicate<T>): ModelProcessor<T, TModel> {
        return { modelData, resourceBundle -> return { entity, consumer -> if (predicate.test(entity)) { installer.create(entity, consumer) } } }
    }
}