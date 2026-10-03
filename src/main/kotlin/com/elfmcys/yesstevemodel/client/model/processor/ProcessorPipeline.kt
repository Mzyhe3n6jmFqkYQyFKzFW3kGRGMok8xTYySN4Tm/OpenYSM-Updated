package com.elfmcys.yesstevemodel.client.model.processor

import com.elfmcys.yesstevemodel.client.entity.GeoEntity
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import java.util.*
import java.util.function.Consumer

open class ProcessorPipeline<T : GeoEntity<*>, TModel> {

    private val processors = ReferenceArrayList<ModelProcessor<T, TModel>>()

    open fun isEmpty(): Boolean {
        return processors.isEmpty
    }

    open fun buildAll(modelData: TModel, resourceBundle: ModelResourceBundle): Consumer<T> {
        val installers = ReferenceArrayList<ControllerFactory<T>>(processors.size)
        for (processor in processors) {
            installers.add(processor.process(modelData, resourceBundle))
        }
        return Consumer { entity ->
            Objects.requireNonNull(entity)
            val consumer = Consumer<IAnimationController<T>> { controller ->
                entity.addAnimationController(controller)
            }
            for (installer in installers) {
                installer?.create(entity, consumer)
            }
        }
    }

    open fun register(processor: ModelProcessor<T, TModel>): ModelProcessor<T, TModel> {
        processors.add(processor)
        return processor
    }
}
