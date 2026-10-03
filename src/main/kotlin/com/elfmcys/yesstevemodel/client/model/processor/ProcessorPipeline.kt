package com.elfmcys.yesstevemodel.client.model.processor

import com.elfmcys.yesstevemodel.client.entity.GeoEntity
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import java.util.Objects
import java.util.function.Consumer

open class ProcessorPipeline<T, TModel> {
    val processors: ReferenceArrayList<ModelProcessor<T, TModel>> = ReferenceArrayList()
    open fun isEmpty(): Boolean {
        return this.processors.isEmpty()
    }
    open fun buildAll(modelData: TModel, resourceBundle: ModelResourceBundle): Consumer<T> {
        var installers: ReferenceArrayList<ControllerFactory<T>> = ReferenceArrayList(this.processors.size())
        for (processor in this.processors) {
            installers.add(processor.process(modelData, resourceBundle))
        }
        return { entity -> Objects.requireNonNull(entity)
 }
    }
    open fun register(processor: ModelProcessor<T, TModel>): ModelProcessor<T, TModel> {
        this.processors.add(processor)
        return processor
    }
}