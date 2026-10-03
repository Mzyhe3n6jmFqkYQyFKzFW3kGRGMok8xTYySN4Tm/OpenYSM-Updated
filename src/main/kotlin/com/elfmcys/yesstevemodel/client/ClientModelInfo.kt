package com.elfmcys.yesstevemodel.client

import com.elfmcys.yesstevemodel.client.model.MainModelData
import com.elfmcys.yesstevemodel.client.texture.OuterFileTexture
import com.elfmcys.yesstevemodel.geckolib3.file.ModelExtraResourcesFile
import com.elfmcys.yesstevemodel.geckolib3.file.ProjectileModelFiles
import com.elfmcys.yesstevemodel.geckolib3.file.VehicleModelFiles
import com.elfmcys.yesstevemodel.model.format.ServerModelInfo

class ClientModelInfo(
    val mainModelData: MainModelData,
    val projectileModelFiles: Array<ProjectileModelFiles>,
    val vehicleModelFiles: Array<VehicleModelFiles>,
    val extraResources: ModelExtraResourcesFile,
    val info: ServerModelInfo,
    val avatarTextures: MutableMap<String, OuterFileTexture>,
    val guiTextures: MutableMap<String, OuterFileTexture>
) {
    fun getMainModelData(): MainModelData = mainModelData
    fun getExtraItemModels(): Array<ProjectileModelFiles> = projectileModelFiles
    fun getVehicleModelFiles(): Array<VehicleModelFiles> = vehicleModelFiles
    fun getExtraResources(): ModelExtraResourcesFile = extraResources
    fun getAvatarTextures(): MutableMap<String, OuterFileTexture> = avatarTextures
    fun getGuiTextures(): MutableMap<String, OuterFileTexture> = guiTextures
    fun getInfo(): ServerModelInfo = info
}
