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
import org.jetbrains.annotations.NotNull
import java.util.function.Consumer

open class MaidAnimatable : LivingAnimatable<EntityMaid>, IGeoEntity {
    var maidModelInfo: MaidModelInfo = null
    constructor(entityMaid: EntityMaid, isActive: Boolean) {
        this.maidModelInfo = MaidModelInfo()
    }
    open fun registerAnimationControllers() {
        (getModelAssembly().getAnimationBundle().getMaidControllerInstaller() as Consumer).accept(this)
    }
    open fun buildRenderShape(modelAssembly: ModelAssembly, isActive: Boolean): GeoEntity {
        TexturedModelWrapper(modelAssembly, isActive, true, true, 600)
    }
    open fun createPositionTracker(entityMaid: EntityMaid): MaidFrameState {
        MaidFrameState(entityMaid)
    }
    open fun getPositionTracker(): MaidFrameState {
        (super.getPositionTracker() as MaidFrameState)
    }
    open fun hasModel(): Boolean {
        this.entity.rouletteAnimDirty
    }
    open fun refreshModel() {
        this.entity.rouletteAnimDirty = false
    }
    open fun isModelAvailable(): Boolean {
        this.entity.rouletteAnimPlaying
    }
    open fun getModelTextureId(): String {
        this.entity.rouletteAnim
    }
    open fun setMolangVars(molangVars: Object2FloatOpenHashMap<String>)
    open fun updateRoamingVars(roamingVars: Object2FloatOpenHashMap<String>)
    open fun getPropertyContainer(): Struct {
        null
    }
    open fun setupAnim(seekTime: Float, isFirstPerson: Boolean) {
        super.setupAnim(seekTime, isFirstPerson)
        getEvaluationContext().setRoamingProperties(getPropertyContainer())
    }
    open fun getMaid(): IMaid {
        this.entity
    }
    open fun getMaidInfo(): MaidModelInfo {
        this.maidModelInfo
    }
    open fun setMaidInfo(maidModelInfo: MaidModelInfo) {
        if (this.maidModelInfo != maidModelInfo) {
            this.maidModelInfo = maidModelInfo
        }
    }
    open fun getGeoModel(): ILocationModel {
        getCurrentModel().getTouhouMaidData()
    }
    open fun setYsmModel(modelId: String, texture: String) {
        initModelWithTexture(modelId, texture)
    }
}