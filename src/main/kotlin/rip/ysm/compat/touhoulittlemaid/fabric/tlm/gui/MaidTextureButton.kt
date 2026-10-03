package rip.ysm.compat.touhoulittlemaid.fabric.tlm.gui

import com.elfmcys.yesstevemodel.client.entity.PlayerPreviewEntity
import com.elfmcys.yesstevemodel.client.gui.button.TextureButton
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.util.ComponentUtil
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import com.github.tartaricacid.touhoulittlemaid.network.message.YsmMaidModelPackage
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.input.InputWithModifiers
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Entity
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidRenderStore

@Environment(EnvType.CLIENT)
open class MaidTextureButton(
    x: Int,
    y: Int,
    previewEntity: PlayerPreviewEntity,
    maid: EntityMaid,
    textureIndex: Int,
    modelAssembly: ModelAssembly
) : TextureButton(x, y, previewEntity, modelAssembly) {

    private val maidId: Int = (maid as Entity).id
    private val modelId: String?
    private val textureName: String?
    private val displayName: Component?

    init {
        val animatable = MaidRenderStore.getOrCreate(maid)
        val assembly: ModelAssembly? = animatable.getModelAssembly()
        this.modelId = animatable.getModelId()
        this.displayName = if (assembly != null && this.modelId != null) {
            ComponentUtil.getDisplayName(assembly, this.modelId)
        } else {
            Component.literal(this.modelId)
        }
        this.textureName = assembly?.animationBundle?.textures?.getKeyAt(textureIndex)
        if (this.modelId != null && this.textureName != null) {
            previewEntity.initModelWithTexture(this.modelId, this.textureName)
        }
    }

    override fun onPress(input: InputWithModifiers) {
        if (modelId == null || textureName == null || displayName == null) {
            return
        }
        ClientPlayNetworking.send(YsmMaidModelPackage(maidId, modelId, textureName, displayName))
    }
}
