package com.elfmcys.yesstevemodel.client.model.processor

import com.elfmcys.yesstevemodel.client.entity.GeoEntity
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import java.util.*

open class ProcessorPipeline<T : GeoEntity<*>, TModel> {
    private val processors = ReferenceArrayList<ModelProcessor<T, TModel>>()

    open fun isEmpty(): Boolean = processors.isEmpty

    open fun buildAll(modelData: TModel, resourceBundle: ModelResourceBundle): (T) -> Unit {
        val installers = ReferenceArrayList<ControllerFactory<T>>(processors.size)
        for (processor in processors) installers.add(processor.process(modelData, resourceBundle))
        return { entity ->
            Objects.requireNonNull(entity)
            val consumer: (IAnimationController<T>) -> Unit = { controller ->
                entity.addAnimationController(controller)
            }
            for (installer in installers) installer?.create(entity, consumer)
        }
    }

    open fun register(processor: ModelProcessor<T, TModel>): ModelProcessor<T, TModel> {
        processors.add(processor)
        return processor
    }
}
