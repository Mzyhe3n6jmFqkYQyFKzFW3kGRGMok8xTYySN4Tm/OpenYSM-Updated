package com.elfmcys.yesstevemodel.client.entity

import com.elfmcys.yesstevemodel.client.ClientModelManager
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionManager
import com.elfmcys.yesstevemodel.client.animation.molang.MolangEventDispatcher
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.client.upload.IResourceLocatable
import com.elfmcys.yesstevemodel.client.upload.UploadManager
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel
import com.elfmcys.yesstevemodel.geckolib3.model.provider.data.EntityModelData
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap
import it.unimi.dsi.fastutil.booleans.BooleanArrayList
import it.unimi.dsi.fastutil.booleans.BooleanList
import net.minecraft.client.renderer.texture.AbstractTexture
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.LivingEntity
import org.joml.Vector2f

abstract class LivingAnimatable<T : LivingEntity>(
    t: T,
    isActive: Boolean
) : GeoEntity<T>(t, isActive) {
    @JvmField
    var currentTextureName: String? = null
    private var textureIndex: Int = 0
    private val armorBoneOffset: Vector2f = Vector2f()
    private var needsInit: Boolean = false
    private var playerUpdateIValue: IValue? = null
    private val updateExpressionArgs: BooleanList = BooleanArrayList(1).apply { size(1) }
    private var forceDisabled: Boolean = false
    private var extraRenderFlag: Boolean = false

    override fun applyHeadTracking(event: AnimationEvent<AnimatableEntity<T>>, wasAnimEvaluated: Boolean) {
        val model: AnimatedGeoModel? = currentModel
        if (model != null && model.headBones().isNotEmpty()) {
            val bone: IBone = model.headBones()[model.headBones().size - 1]
            if (wasAnimEvaluated) {
                armorBoneOffset.set(bone.getRotationX(), bone.getRotationY())
            }
            val data: EntityModelData = event.modelData
            bone.setRotationX(armorBoneOffset.x + Math.toRadians(data.headPitch.toDouble()).toFloat())
            bone.setRotationY(armorBoneOffset.y + Math.toRadians(data.netHeadYaw.toDouble()).toFloat())
        }
    }

    override fun resetHeadTracking(wasAnimEvaluated: Boolean) {
        val model: AnimatedGeoModel? = currentModel
        if (model != null && model.headBones().isNotEmpty()) {
            val bone: IBone = model.headBones()[model.headBones().size - 1]
            bone.setRotationX(armorBoneOffset.x)
            bone.setRotationY(armorBoneOffset.y)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun createPositionTracker(t: T): LivingEntityFrameState<T> {
        return LivingEntityFrameState(t)
    }

    @Suppress("UNCHECKED_CAST")
    override fun getPositionTracker(): LivingEntityFrameState<T> {
        return super.getPositionTracker() as LivingEntityFrameState<T>
    }

    open fun setCurrentTexture(str: String?) {
        currentTextureName = str
        updateCurrentTexture()
    }

    open fun initModelWithTexture(str: String, str2: String?) {
        markModelInitialized()
        currentTextureName = str2
        setModelId(str)
        updateCurrentTexture()
    }

    open fun setForceDisabled(forceDisabled: Boolean) {
        this.forceDisabled = forceDisabled
    }

    open fun isForceDisabled(): Boolean {
        return forceDisabled
    }

    open fun isModelActive(): Boolean {
        return isModelInitialized() && !forceDisabled
    }

    override fun onModelLoaded(context: ModelAssembly) {
        super.onModelLoaded(context)
        updateCurrentTexture()
        val values: List<IValue>? = context.expressionCache.events[MolangEventDispatcher.PLAYER_UPDATE]
        playerUpdateIValue =
            if (values != null) MolangEventDispatcher.createUpdateExpression(values, updateExpressionArgs) else null
    }

    override fun setCurrentModel(model: AnimatedGeoModel?) {
        super.setCurrentModel(model)
        if (model != null && model.headBones().isNotEmpty()) {
            val bone: IBone = model.headBones()[model.headBones().size - 1]
            armorBoneOffset.set(bone.getRotationX(), bone.getRotationY())
        }
    }

    override fun resetModel() {
        super.resetModel()
        currentTextureName = null
        textureIndex = 0
        forceDisabled = false
    }

    override fun reset() {
        super.reset()
        armorBoneOffset.set(0.0f)
        extraRenderFlag = false
        needsInit = true
    }

    override fun setupAnim(seekTime: Float, isFirstPerson: Boolean) {
        super.setupAnim(seekTime, isFirstPerson)
        if (needsInit) {
            needsInit = false
            val values: List<IValue>? = getAnimationExpressions(MolangEventDispatcher.PLAYER_INIT)
            if (values != null) {
                executeExpression(MolangEventDispatcher.createInitExpression(values), true, true, null)
            }
        }
        val updateExp = playerUpdateIValue
        if (updateExp != null) {
            updateExpressionArgs.set(0, isFirstPerson)
            executeExpression(updateExp, true, true, null)
        }
    }

    open fun getModelConfig(): ConditionManager? {
        return getModelAssembly()?.animationBundle?.conditionManager
    }

    private fun updateCurrentTexture() {
        if (isModelReady()) {
            val map: OrderedStringMap<String, out AbstractTexture>? =
                getModelAssembly()?.animationBundle?.textures
            if (map != null) {
                val abstractTexture: AbstractTexture? = map[currentTextureName]
                if (abstractTexture != null) {
                    (getRenderShape() as? LivingAnimatable<*>.TexturedModelWrapper)?.setTexture(abstractTexture)
                    textureIndex = map.getValuesList().indexOf(abstractTexture)
                } else if (!map.isEmpty()) {
                    currentTextureName = map.getKeyAt(0)
                    map.getValueAt(0)
                        .let { (getRenderShape() as? LivingAnimatable<*>.TexturedModelWrapper)?.setTexture(it) }
                    textureIndex = 0
                }
            }
        }
    }

    override fun getAnimationProcessor(): GeoModel = getModelAssembly()!!.animationBundle.mainModel

    override fun getAnimation(str: String): Animation? = getModelAssembly()?.animationBundle?.mainAnimations?.get(str)

    override fun getAnimationEntries(str: String): AnimationController? =
        getModelAssembly()?.animationBundle?.animationEntries?.get(str)

    open fun getCurrentTextureName(): String? {
        return if (isModelReady()) currentTextureName else getModelAssembly()?.animationBundle?.textures
            ?.getKeyAt(0)
    }

    override fun getTextureLocation(): Identifier {
        return if (isModelReady()) {
            (getRenderShape() as? LivingAnimatable<*>.TexturedModelWrapper)?.currentTexture?.getResourceLocation()
                ?: ClientModelManager.getDefaultTexture()
        } else {
            ClientModelManager.getDefaultTexture()
        }
    }

    override fun getTextureIndex(): Int {
        if (isModelReady()) {
            return textureIndex
        }
        return 0
    }

    override fun getWidthScale(): Float {
        return getModelAssembly()?.modelData?.modelProperties?.widthScale ?: 1.0f
    }

    override fun getHeightScale(): Float {
        return getModelAssembly()?.modelData?.modelProperties?.heightScale ?: 1.0f
    }

    open fun isRenderLayersFirst(): Boolean {
        return getModelAssembly()?.modelData?.modelProperties?.renderLayersFirst ?: false
    }

    open fun isExtraRenderFlag(): Boolean {
        return extraRenderFlag
    }

    open fun setExtraRenderFlag(extraRenderFlag: Boolean) {
        this.extraRenderFlag = extraRenderFlag
    }

    open inner class TexturedModelWrapper(
        modelAssembly: ModelAssembly,
        isActive: Boolean,
        collectAllTextures: Boolean,
        registerImmediately: Boolean,
        private val textureResolution: Int
    ) : ModelWrapper(modelAssembly, isActive) {
        @JvmField
        var currentTexture: IResourceLocatable?
        val allTextures: MutableList<IResourceLocatable>?

        init {
            val abstractTexture =
                modelAssembly.animationBundle.textures[this@LivingAnimatable.currentTextureName]
                    ?: modelAssembly.animationBundle.defaultTexture
            currentTexture = if (abstractTexture != null) {
                UploadManager.getOrCreateLocatableWithSize(abstractTexture, registerImmediately, textureResolution)
            } else {
                null
            }
            if (collectAllTextures) {
                val list = ArrayList<IResourceLocatable>()
                for (texture in modelAssembly.animationBundle.textures.values) {
                    list.add(UploadManager.getOrCreateLocatable(texture, false))
                }
                for (projectileModelBundle in modelAssembly.projectileModels.values) {
                    list.add(UploadManager.getOrCreateLocatable(projectileModelBundle.texture, false))
                }
                for (vehicleModelBundle in modelAssembly.vehicleModels.values) {
                    list.add(UploadManager.getOrCreateLocatable(vehicleModelBundle.texture, false))
                }
                allTextures = list
            } else {
                allTextures = null
            }
        }

        fun setTexture(abstractTexture: AbstractTexture) {
            currentTexture = UploadManager.getOrCreateLocatableWithSize(abstractTexture, true, textureResolution)
        }

        override fun isValid(): Boolean = currentTexture?.getResourceLocation() != null
    }
}