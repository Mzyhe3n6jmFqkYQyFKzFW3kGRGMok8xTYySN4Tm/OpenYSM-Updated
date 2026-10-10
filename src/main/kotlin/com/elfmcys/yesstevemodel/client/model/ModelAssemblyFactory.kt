package com.elfmcys.yesstevemodel.client.model

import com.elfmcys.yesstevemodel.client.ClientModelInfo
import com.elfmcys.yesstevemodel.client.animation.condition.ArmorConditions
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionManager
import com.elfmcys.yesstevemodel.client.gui.metadata.ModelDisplayAssets
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.elfmcys.yesstevemodel.util.FileTypeUtil
import it.unimi.dsi.fastutil.objects.*
import net.minecraft.client.renderer.texture.AbstractTexture
import net.minecraft.resources.Identifier

object ModelAssemblyFactory {
    private const val FIRST_PERSON_ARM_BONE: String = "fp_arm"

    var primaryAssembly: ModelAssembly? = null

    fun buildAssembly(clientModelInfo: ClientModelInfo, isPrimary: Boolean, isAuth: Boolean): ModelAssembly {
        val textureList = ArrayList<AbstractTexture>()
        val resourceBundle = buildResourceBundle(clientModelInfo)
        val assembly = ModelAssembly(
            buildPlayerModelBundle(clientModelInfo, resourceBundle, isPrimary, textureList),
            buildProjectileModels(clientModelInfo, resourceBundle, isPrimary, textureList),
            buildVehicleModels(clientModelInfo, resourceBundle, isPrimary, textureList),
            resourceBundle,
            clientModelInfo.info,
            buildTextureRegistry(clientModelInfo, isAuth, textureList),
            textureList
        )
        if (isPrimary) {
            primaryAssembly = assembly
            assembly.animationBundle.mainAnimations.values.forEach { animation ->
                animation.isFromPrimaryAssembly = true
            }
        }
        return assembly
    }

    fun buildPlayerModelBundle(
        clientModelInfo: ClientModelInfo,
        resourceBundle: ModelResourceBundle,
        isPrimary: Boolean,
        textureList: MutableList<AbstractTexture>
    ): PlayerModelBundle {
        val hierarchyData = clientModelInfo.mainModelData
        val mainModel = hierarchyData.models[0]
        val armModel = hierarchyData.models[1]
        val object2ReferenceOpenHashMap = Object2ReferenceLinkedOpenHashMap<String, Animation>()
        val armAnimations = Object2ReferenceLinkedOpenHashMap<String, Animation>()
        for ((str, animationFile) in hierarchyData.animations) {
            for (animation in animationFile.animations.values) {
                if (animation.sourceKey == null) {
                    animation.sourceKey = str
                }
            }
            if (FIRST_PERSON_ARM_BONE == str) armAnimations.putAll(animationFile.animations) else
                object2ReferenceOpenHashMap.putAll(animationFile.animations)
        }
        if (!isPrimary) {
            primaryAssembly?.let { primary ->
                for ((key, value) in primary.animationBundle.mainAnimations) {
                    object2ReferenceOpenHashMap.putIfAbsent(key, value)
                }
                for ((key, value) in primary.animationBundle.armAnimations) {
                    armAnimations.putIfAbsent(key, value)
                }
            }
        }
        val conditionManager = ConditionManager()
        object2ReferenceOpenHashMap.keys.forEach(conditionManager::addTest)
        val armorRegistry = ArmorConditions()
        armAnimations.keys.forEach(armorRegistry::addCondition)
        val animationControllers = Object2ReferenceOpenHashMap<String, AnimationController>()
        for (animationControllerFile in hierarchyData.animationControllers) {
            animationControllers.putAll(animationControllerFile.animationControllers)
        }
        for (texture in hierarchyData.textureMap.values) {
            textureList.add(texture)
            textureList.addAll(texture.suffixTextures.values)
        }
        var defaultTextureName = clientModelInfo.info.modelProperties.defaultTexture
        if (defaultTextureName.isEmpty() || !hierarchyData.textureMap.containsKey(defaultTextureName))
            defaultTextureName = if (hierarchyData.textureMap.isEmpty()) "" else hierarchyData.textureMap.getKeyAt(0)
        return PlayerModelBundle(
            mainModel,
            armModel,
            object2ReferenceOpenHashMap,
            armAnimations,
            conditionManager,
            armorRegistry,
            animationControllers,
            hierarchyData.textureMap,
            defaultTextureName,
            hierarchyData.textureMap[defaultTextureName],
            resourceBundle
        )
    }

    fun buildProjectileModels(
        clientModelInfo: ClientModelInfo,
        resourceBundle: ModelResourceBundle,
        isPrimary: Boolean,
        textureList: MutableList<AbstractTexture>
    ): MutableMap<Identifier, ProjectileModelBundle> {
        val projectileMap = Object2ReferenceOpenHashMap<Identifier, ProjectileModelBundle>()
        for (projectileFiles in clientModelInfo.projectileModelFiles) {
            val model = projectileFiles.model
            val animationFile = projectileFiles.animations
            val controllerFile = projectileFiles.animationController
            val animations = Object2ReferenceOpenHashMap(animationFile.animations)
            val controllers =
                Object2ReferenceOpenHashMap(controllerFile.animationControllers)
            textureList.add(projectileFiles.texture)
            textureList.addAll(projectileFiles.texture.suffixTextures.values)
            val projectileBundle =
                ProjectileModelBundle(model, animations, controllers, projectileFiles.texture, resourceBundle)
            for (identifier in FileTypeUtil.resolveEntityTypes(projectileFiles.textureNames)) {
                projectileMap[identifier] = projectileBundle
            }
        }
        return projectileMap
    }

    fun buildVehicleModels(
        clientModelInfo: ClientModelInfo,
        resourceBundle: ModelResourceBundle,
        isPrimary: Boolean,
        textureList: MutableList<AbstractTexture>
    ): MutableMap<Identifier, VehicleModelBundle> {
        val vehicleMap = Object2ReferenceOpenHashMap<Identifier, VehicleModelBundle>()
        for (vehicleFiles in clientModelInfo.vehicleModelFiles) {
            val model = vehicleFiles.model
            val animationFile = vehicleFiles.animations
            val controllerFile = vehicleFiles.animationController
            val animations = Object2ReferenceOpenHashMap(animationFile.animations)
            val controllers = Object2ReferenceOpenHashMap(controllerFile.animationControllers)
            textureList.add(vehicleFiles.texture)
            textureList.addAll(vehicleFiles.texture.suffixTextures.values)
            val vehicleBundle = VehicleModelBundle(model, animations, controllers, vehicleFiles.texture, resourceBundle)
            for (identifier in FileTypeUtil.resolveEntityTypes(vehicleFiles.textureNames)) {
                vehicleMap[identifier] = vehicleBundle
            }
        }
        return vehicleMap
    }

    fun buildResourceBundle(clientModelInfo: ClientModelInfo): ModelResourceBundle {
        return ModelResourceBundle(
            clientModelInfo.extraResources.audioTracks,
            buildMolangFunctions(clientModelInfo),
            extractMolangEvents(clientModelInfo),
            clientModelInfo.extraResources.translations
        )
    }

    fun buildTextureRegistry(
        clientModelInfo: ClientModelInfo,
        isAuth: Boolean,
        textureList: MutableList<AbstractTexture>
    ): ModelDisplayAssets {
        val extraTextures = extractExtraTextures(clientModelInfo, textureList)
        val metadata = clientModelInfo.info.metadata
        return ModelDisplayAssets(
            metadata?.name ?: StringPool.EMPTY,
            isAuth,
            clientModelInfo.avatarTextures,
            extraTextures as MutableMap<String, AbstractTexture>
        )
    }

    fun buildMolangFunctions(clientModelInfo: ClientModelInfo): Object2ReferenceOpenHashMap<String, IValue> {
        val functions = Object2ReferenceOpenHashMap<String, IValue>(clientModelInfo.extraResources.functions.size)
        for ((rawKey, value) in clientModelInfo.extraResources.functions) {
            val atIndex = rawKey.indexOf('@')
            if (atIndex != 0) {
                val key = if (atIndex != -1) rawKey.substring(0, atIndex) else rawKey
                functions[key] = value
            }
        }
        return functions
    }

    fun extractMolangEvents(clientModelInfo: ClientModelInfo): Object2ReferenceOpenHashMap<String, MutableList<IValue>> {
        val events = Object2ReferenceOpenHashMap<String, MutableList<IValue>>()
        for ((key, value) in clientModelInfo.extraResources.functions) {
            val atIndex = key.indexOf('@')
            if (atIndex != -1 && atIndex + 1 < key.length) {
                val eventName = key.substring(atIndex + 1).lowercase()
                events.computeIfAbsent(eventName) { ReferenceArrayList() }.add(value)
            }
        }
        return events
    }

    fun extractExtraTextures(
        clientModelInfo: ClientModelInfo,
        textureList: MutableList<AbstractTexture>
    ): Map<String, AbstractTexture> {
        val extraTextures = Object2ObjectOpenHashMap<String, AbstractTexture>()
        for ((key, texture) in clientModelInfo.guiTextures) {
            textureList.add(texture)
            extraTextures[key] = texture
        }
        return Object2ObjectMaps.unmodifiable(extraTextures)
    }
}