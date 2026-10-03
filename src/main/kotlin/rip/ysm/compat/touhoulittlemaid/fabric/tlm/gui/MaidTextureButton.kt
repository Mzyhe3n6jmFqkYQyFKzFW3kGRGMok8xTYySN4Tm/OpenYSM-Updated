package rip.ysm.compat.touhoulittlemaid.fabric.tlm.gui

import com.elfmcys.yesstevemodel.client.entity.PlayerPreviewEntity
import com.elfmcys.yesstevemodel.client.gui.button.TextureButton
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.util.ComponentUtil
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import com.github.tartaricacid.touhoulittlemaid.network.message.YsmMaidModelPackage
import net.minecraft.world.entity.Entity
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.input.InputWithModifiers
import net.minecraft.network.chat.Component
import org.jetbrains.annotations.Nullable
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidRenderStore

open class MaidTextureButton : TextureButton {
    var maidId: Int = 0
    var modelId: String? = null
    var textureName: String? = null
    var displayName: Component? = null
    constructor(x: Int, y: Int, previewEntity: PlayerPreviewEntity, maid: EntityMaid, textureIndex: Int, modelAssembly: ModelAssembly) {
        this.maidId = (maid as Entity).getId()
        var animatable: Any = MaidRenderStore.getOrCreate(maid)
        var assembly: ModelAssembly = animatable.getModelAssembly()
        this.modelId = animatable.getModelId()
        this.displayName = ComponentUtil.getDisplayName(assembly, this.modelId)
        this.textureName = assembly.getAnimationBundle().getTextures().getKeyAt(textureIndex)
        previewEntity.initModelWithTexture(this.modelId, this.textureName)
    }
    open fun onPress(input: InputWithModifiers) {
        if (this.modelId == null || this.textureName == null || this.displayName == null) {
             }
        ClientPlayNetworking.send(YsmMaidModelPackage(this.maidId, this.modelId, this.textureName, this.displayName))
    }
}