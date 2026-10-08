package com.elfmcys.yesstevemodel.client

import com.elfmcys.yesstevemodel.client.model.MainModelData
import com.elfmcys.yesstevemodel.client.texture.OuterFileTexture
import com.elfmcys.yesstevemodel.geckolib3.file.ModelExtraResourcesFile
import com.elfmcys.yesstevemodel.geckolib3.file.ProjectileModelFiles
import com.elfmcys.yesstevemodel.geckolib3.file.VehicleModelFiles
import com.elfmcys.yesstevemodel.model.format.ServerModelInfo

data class ClientModelInfo(
    val mainModelData: MainModelData,
    val projectileModelFiles: Array<ProjectileModelFiles>,
    val vehicleModelFiles: Array<VehicleModelFiles>,
    val extraResources: ModelExtraResourcesFile,
    val info: ServerModelInfo,
    val avatarTextures: MutableMap<String, OuterFileTexture>,
    val guiTextures: MutableMap<String, OuterFileTexture>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClientModelInfo

        if (mainModelData != other.mainModelData) return false
        if (!projectileModelFiles.contentEquals(other.projectileModelFiles)) return false
        if (!vehicleModelFiles.contentEquals(other.vehicleModelFiles)) return false
        if (extraResources != other.extraResources) return false
        if (info != other.info) return false
        if (avatarTextures != other.avatarTextures) return false
        if (guiTextures != other.guiTextures) return false

        return true
    }

    override fun hashCode(): Int {
        var result = mainModelData.hashCode()
        result = 31 * result + projectileModelFiles.contentHashCode()
        result = 31 * result + vehicleModelFiles.contentHashCode()
        result = 31 * result + extraResources.hashCode()
        result = 31 * result + info.hashCode()
        result = 31 * result + avatarTextures.hashCode()
        result = 31 * result + guiTextures.hashCode()
        return result
    }
}
