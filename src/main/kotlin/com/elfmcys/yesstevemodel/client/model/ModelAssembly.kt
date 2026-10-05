@file:Suppress("unused")

package com.elfmcys.yesstevemodel.client.model

import com.elfmcys.yesstevemodel.client.gui.ModelMetadataPresenter
import com.elfmcys.yesstevemodel.client.gui.metadata.ModelDisplayAssets
import com.elfmcys.yesstevemodel.model.format.ServerModelInfo
import net.minecraft.client.renderer.texture.AbstractTexture
import net.minecraft.resources.Identifier

data class ModelAssembly(
    val animationBundle: PlayerModelBundle,
    val projectileModels: Map<Identifier, ProjectileModelBundle>,
    val vehicleModels: Map<Identifier, VehicleModelBundle>,
    val expressionCache: ModelResourceBundle,
    val modelData: ServerModelInfo,
    val textureRegistry: ModelDisplayAssets,
    val textures: List<AbstractTexture>
) {
    fun getDisplayName(defaultName: String): String {
        val metadata = modelData.metadata ?: return defaultName
        return ModelMetadataPresenter.getLocalizedModelString(this, "metadata.name", metadata.name)
    }
}