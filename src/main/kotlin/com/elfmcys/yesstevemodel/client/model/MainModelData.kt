package com.elfmcys.yesstevemodel.client.model

import com.elfmcys.yesstevemodel.client.texture.OuterFileTexture
import com.elfmcys.yesstevemodel.geckolib3.file.AnimationControllerFile
import com.elfmcys.yesstevemodel.geckolib3.file.AnimationFile
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList

open class MainModelData(
    models: Array<GeoModel>,
    val animations: Map<String, AnimationFile>,
    animationControllerFiles: Array<AnimationControllerFile>,
    val textureMap: OrderedStringMap<String, OuterFileTexture>
) {
    val models: List<GeoModel> = ObjectArrayList.wrap(models)
    val animationControllers: List<AnimationControllerFile> = ObjectArrayList.wrap(animationControllerFiles)
}