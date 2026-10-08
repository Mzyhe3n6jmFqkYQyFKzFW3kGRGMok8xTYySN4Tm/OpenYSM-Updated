@file:Suppress("unused")

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
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel
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
    private var currentTextureName2: String? = null
    private var textureIndex2: Int = 0
    private val armorBoneOffset: Vector2f = Vector2f()
    private var needsInit: Boolean = false
    private var playerUpdateIValue: IValue? = null
    private val updateExpressionArgs: BooleanList = BooleanArrayList(1).apply { size(1) }
    private var forceDisabled2: Boolean = false
    private var extraRenderFlag: Boolean = false

    override fun applyHeadTracking(event: AnimationEvent<AnimatableEntity<T>>, z: Boolean) {
        val model = currentModel
        if (model != null && model.headBones.isNotEmpty()) {
            val bone = model.headBones[model.headBones.size - 1]
            if (z) armorBoneOffset.set(bone.rotationX, bone.rotationY)
            val data = event.modelData
            bone.rotationX = armorBoneOffset.x + Math.toRadians(data.headPitch.toDouble()).toFloat()
            bone.rotationY = armorBoneOffset.y + Math.toRadians(data.netHeadYaw.toDouble()).toFloat()
        }
    }

    override fun resetHeadTracking(wasAnimEvaluated: Boolean) {
        val model = currentModel
        if (model != null && model.headBones.isNotEmpty()) {
            val bone = model.headBones[model.headBones.size - 1]
            bone.rotationX = armorBoneOffset.x
            bone.rotationY = armorBoneOffset.y
        }
    }

    override fun createPositionTracker(entity: T): LivingEntityFrameState<T> = LivingEntityFrameState(entity)

    override val positionTracker: LivingEntityFrameState<T>
        get() = super.positionTracker as LivingEntityFrameState<T>

    open var currentTexture: String?
        get() = currentTextureName2
        set(value) {
            currentTextureName2 = value
            updateCurrentTexture()
        }

    open fun initModelWithTexture(str: String, str2: String?) {
        markModelInitialized()
        currentTextureName2 = str2
        modelId = str
        updateCurrentTexture()
    }

    open fun setForceDisabled(forceDisabled: Boolean) {
        forceDisabled2 = forceDisabled
    }

    open val isForceDisabled: Boolean
        get() = forceDisabled2

    open val isModelActive: Boolean
        get() = isModelInitialized && !forceDisabled2

    override fun onModelLoaded(modelAssembly: ModelAssembly) {
        super.onModelLoaded(modelAssembly)
        updateCurrentTexture()
        val values = modelAssembly.expressionCache.events[MolangEventDispatcher.PLAYER_UPDATE]
        playerUpdateIValue =
            if (values != null) MolangEventDispatcher.createUpdateExpression(values, updateExpressionArgs) else null
    }

    override var currentModel: AnimatedGeoModel?
        get() = super.currentModel
        set(value) {
            super.currentModel = value
            if (value != null && value.headBones.isNotEmpty()) {
                val bone = value.headBones[value.headBones.size - 1]
                armorBoneOffset.set(bone.rotationX, bone.rotationY)
            }
        }

    override fun resetModel() {
        super.resetModel()
        currentTextureName2 = null
        textureIndex2 = 0
        forceDisabled2 = false
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
            val values = getAnimationExpressions(MolangEventDispatcher.PLAYER_INIT)
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

    open val modelConfig: ConditionManager?
        get() = modelAssembly?.animationBundle?.conditionManager

    private fun updateCurrentTexture() {
        if (isModelReady) {
            val map =
                modelAssembly?.animationBundle?.textures
            if (map != null) {
                val abstractTexture = map[currentTextureName2]
                when {
                    abstractTexture != null -> {
                        (renderShape as? LivingAnimatable<*>.TexturedModelWrapper)?.setTexture(abstractTexture)
                        textureIndex2 = map.getValuesList().indexOf(abstractTexture)
                    }

                    !map.isEmpty() -> {
                        currentTextureName2 = map.getKeyAt(0)
                        map.getValueAt(0)
                            .let { (renderShape as? LivingAnimatable<*>.TexturedModelWrapper)?.setTexture(it) }
                        textureIndex2 = 0
                    }
                }
            }
        }
    }

    override val model: GeoModel
        get() = modelAssembly!!.animationBundle.mainModel

    override fun getAnimation(str: String): Animation? = modelAssembly?.animationBundle?.mainAnimations?.get(str)

    override fun getAnimationEntries(str: String): AnimationController? =
        modelAssembly?.animationBundle?.animationEntries?.get(str)

    open val currentTextureName: String?
        get() = if (isModelReady) currentTextureName2 else modelAssembly?.animationBundle?.textures?.getKeyAt(0)

    override val textureLocation: Identifier
        get() = if (isModelReady) {
            (renderShape as? LivingAnimatable<*>.TexturedModelWrapper)?.texture?.getResourceLocation()
                ?: ClientModelManager.defaultTexture
        } else {
            ClientModelManager.defaultTexture
        }

    override val textureIndex: Int
        get() {
            if (isModelReady) return textureIndex2
            return 0
        }

    override val widthScale: Float
        get() = modelAssembly?.modelData?.modelProperties?.widthScale ?: 1.0f

    override val heightScale: Float
        get() = modelAssembly?.modelData?.modelProperties?.heightScale ?: 1.0f

    open val isRenderLayersFirst: Boolean
        get() = modelAssembly?.modelData?.modelProperties?.renderLayersFirst ?: false

    open val isExtraRenderFlag: Boolean
        get() = extraRenderFlag

    open fun setExtraRenderFlag(extraRenderFlag: Boolean) {
        this.extraRenderFlag = extraRenderFlag
    }

    @Suppress("MemberVisibilityCanBePrivate")
    open inner class TexturedModelWrapper(
        modelAssembly: ModelAssembly,
        isActive: Boolean,
        collectAllTextures: Boolean,
        registerImmediately: Boolean,
        private val textureResolution: Int
    ) : ModelWrapper(modelAssembly, isActive) {
        private var currentTexture: IResourceLocatable?
        private val allTextures: MutableList<IResourceLocatable>?

        init {
            val abstractTexture =
                modelAssembly.animationBundle.textures[this@LivingAnimatable.currentTextureName2]
                    ?: modelAssembly.animationBundle.defaultTexture
            currentTexture = if (abstractTexture != null)
                UploadManager.getOrCreateLocatableWithSize(
                    abstractTexture,
                    registerImmediately,
                    textureResolution
                ) else null
            if (collectAllTextures) {
                val list = ArrayList<IResourceLocatable>()
                for (texture in modelAssembly.animationBundle.textures.values)
                    list.add(UploadManager.getOrCreateLocatable(texture, false))
                for (projectileModelBundle in modelAssembly.projectileModels.values)
                    list.add(UploadManager.getOrCreateLocatable(projectileModelBundle.texture, false))
                for (vehicleModelBundle in modelAssembly.vehicleModels.values)
                    list.add(UploadManager.getOrCreateLocatable(vehicleModelBundle.texture, false))
                allTextures = list
            } else {
                allTextures = null
            }
        }

        val texture: IResourceLocatable?
            get() = currentTexture

        fun setTexture(abstractTexture: AbstractTexture) {
            currentTexture = UploadManager.getOrCreateLocatableWithSize(abstractTexture, true, textureResolution)
        }

        override val isValid: Boolean
            get() = currentTexture?.getResourceLocation() != null
    }
}