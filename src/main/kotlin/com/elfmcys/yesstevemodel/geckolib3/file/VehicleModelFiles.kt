package com.elfmcys.yesstevemodel.geckolib3.file

import com.elfmcys.yesstevemodel.client.texture.OuterFileTexture
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel

class VehicleModelFiles(
    val textureNames: Array<String>,
    val model: GeoModel,
    val animations: AnimationFile,
    val animationController: AnimationControllerFile,
    val texture: OuterFileTexture
)