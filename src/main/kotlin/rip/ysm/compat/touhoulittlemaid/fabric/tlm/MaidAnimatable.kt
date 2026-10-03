package rip.ysm.compat.touhoulittlemaid.fabric.tlm

import com.elfmcys.yesstevemodel.client.entity.GeoEntity
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
import java.util.function.Consumer

@Environment(EnvType.CLIENT)
open class MaidAnimatable(entityMaid: EntityMaid, isActive: Boolean) :
    LivingAnimatable<EntityMaid>(entityMaid, isActive), IGeoEntity {

    private var maidModelInfo: MaidModelInfo = MaidModelInfo()

    override fun registerAnimationControllers() {
        getModelAssembly()?.animationBundle?.maidControllerInstaller?.accept(this)
    }

    override fun buildRenderShape(modelAssembly: ModelAssembly, isActive: Boolean): ModelWrapper {
        return TexturedModelWrapper(
            modelAssembly,
            isActive,
            collectAllTextures = true,
            registerImmediately = true,
            textureResolution = 600
        )
    }

    override fun createPositionTracker(entityMaid: EntityMaid): MaidFrameState {
        return MaidFrameState(entityMaid)
    }

    override fun getPositionTracker(): MaidFrameState {
        return super.getPositionTracker() as MaidFrameState
    }

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

    fun getPropertyContainer(): Struct? = null

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

    override fun getGeoModel(): ILocationModel? = currentModel?.getTouhouMaidData<ILocationModel>()

    override fun setYsmModel(modelId: String, texture: String) {
        initModelWithTexture(modelId, texture)
    }
}
