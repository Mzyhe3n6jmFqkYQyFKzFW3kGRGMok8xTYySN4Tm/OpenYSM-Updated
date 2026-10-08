package rip.ysm.compat.touhoulittlemaid.fabric.tlm

import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.molang.runtime.Struct
import com.github.tartaricacid.touhoulittlemaid.api.entity.IMaid
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.IGeoEntity
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.animated.ILocationModel
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment

@Environment(EnvType.CLIENT)
open class MaidAnimatable(entityMaid: EntityMaid, isActive: Boolean) :
    LivingAnimatable<EntityMaid>(entityMaid, isActive), IGeoEntity {

    private var maidModelInfo: MaidModelInfo = MaidModelInfo()

    override fun registerAnimationControllers() {
        modelAssembly?.animationBundle?.maidControllerInstaller?.invoke(this)
    }

    override fun buildRenderShape(modelAssembly: ModelAssembly, isDefault: Boolean): ModelWrapper {
        return TexturedModelWrapper(
            modelAssembly,
            isDefault,
            collectAllTextures = true,
            registerImmediately = true,
            textureResolution = 600
        )
    }

    override fun createPositionTracker(entity: EntityMaid): MaidFrameState {
        return MaidFrameState(entity)
    }

    override val positionTracker: MaidFrameState
        get() = super.positionTracker as MaidFrameState

    fun hasModel(): Boolean = entity.rouletteAnimDirty

    fun refreshModel() {
        entity.rouletteAnimDirty = false
    }

    fun isModelAvailable(): Boolean = entity.rouletteAnimPlaying

    fun getModelTextureId(): String = entity.rouletteAnim

    fun setMolangVars(molangVars: Object2FloatOpenHashMap<String>) {
    }

    override fun updateRoamingVars(roamingVars: Object2FloatOpenHashMap<String>) {
    }

    private fun getPropertyContainer(): Struct? = null

    override fun setupAnim(seekTime: Float, isFirstPerson: Boolean) {
        super.setupAnim(seekTime, isFirstPerson)
        getEvaluationContext().setRoamingProperties(getPropertyContainer())
    }

    override fun getMaid(): IMaid = entity

    override fun getMaidInfo(): MaidModelInfo = maidModelInfo

    override fun setMaidInfo(maidModelInfo: MaidModelInfo) {
        if (this.maidModelInfo != maidModelInfo) {
            this.maidModelInfo = maidModelInfo
        }
    }

    override fun getGeoModel(): ILocationModel? = currentModel2?.getTouhouMaidData<ILocationModel>()

    override fun setYsmModel(modelId: String, texture: String) {
        initModelWithTexture(modelId, texture)
    }
}
