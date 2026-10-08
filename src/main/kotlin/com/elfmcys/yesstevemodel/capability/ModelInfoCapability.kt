@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.elfmcys.yesstevemodel.capability

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.capability.fabric.ModelInfoCapabilityImpl
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.model.ServerModelManager
import com.elfmcys.yesstevemodel.network.message.FeedbackData
import com.elfmcys.yesstevemodel.network.message.S2CSetModelAndTexturePacket
import com.elfmcys.yesstevemodel.network.sync.PlayerStateSynchronizer
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap
import it.unimi.dsi.fastutil.ints.IntSet
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import java.util.*

class ModelInfoCapability {
    private var modelId2: String? = null
    private var selectTexture2: String? = null
    private var mandatory2: Boolean = false
    private var molangStorage: Int2ReferenceOpenHashMap<Object2FloatOpenHashMap<String>> = Int2ReferenceOpenHashMap()
    private var animSync2: PlayerStateSynchronizer = PlayerStateSynchronizer()
    private var disabled2: Boolean = false
    private var dirty: Boolean = false
    private val pendingCallbacks: ArrayDeque<(Object2FloatOpenHashMap<String>) -> Unit> = ArrayDeque()

    var modelId: String
        get() = modelId2 ?: ServerModelManager.getDefaultModelConfig().getLeft()
        set(value) {
            if (modelId2 == value) return
            modelId2 = value
            markDirty()
        }

    val selectTexture: String
        get() = selectTexture2 ?: ServerModelManager.getDefaultModelConfig().getRight()

    fun setSelectTexture(str: String) {
        if (selectTexture2 == str) return
        selectTexture2 = str
        markDirty()
    }

    fun setModelAndTexture(str: String, str2: String) {
        if (modelId == str && selectTexture == str2) return
        modelId2 = str
        selectTexture2 = str2
        markDirty()
    }

    fun resetToDefault() {
        val pair = ServerModelManager.getDefaultModelConfig()
        setModelAndTexture(pair.getLeft(), pair.getRight())
    }

    fun copyFrom(source: ModelInfoCapability) {
        molangStorage = source.molangStorage
        modelId2 = source.modelId2
        selectTexture2 = source.selectTexture2
        mandatory2 = source.mandatory2
        animSync2 = source.animSync2
        pendingCallbacks.addAll(source.pendingCallbacks)
        disabled2 = source.disabled2
        source.pendingCallbacks.clear()
        markDirty()
    }

    fun setDisabled(disabled: Boolean) {
        if (this.disabled2 == disabled) return
        this.disabled2 = disabled
        markDirty()
    }

    fun playAnimation(serverPlayer: ServerPlayer, str: String) {
        animSync2.syncModelSwitch(serverPlayer, !dirty, str)
    }

    fun stopAnimation(serverPlayer: ServerPlayer) {
        animSync2.syncModelSwitch(serverPlayer, !dirty, StringPool.EMPTY)
    }

    fun createSyncMessage(serverPlayer: ServerPlayer, fullSync: Boolean): S2CSetModelAndTexturePacket? =
        ServerModelManager[modelId]?.let {
            val molangVars =
                molangStorage.computeIfAbsent(it.getLoadedModelData().hashId) { Object2FloatOpenHashMap(0) }

            while (true) {
                val callback = pendingCallbacks.poll()
                if (callback != null) callback(molangVars) else break
            }

            S2CSetModelAndTexturePacket(
                serverPlayer.id,
                modelId,
                selectTexture,
                disabled2,
                animSync2.buildFullSyncMessage(serverPlayer, fullSync)
                    .setMolangVars(it.getLoadedModelData().hashId, molangVars)
            )
        }

    fun withMolangVars(consumer: (Object2FloatOpenHashMap<String>) -> Unit) {
        ServerModelManager[modelId]?.let {
            consumer(molangStorage.computeIfAbsent(it.getLoadedModelData().hashId) {
                Object2FloatOpenHashMap(0)
            })
        } ?: pendingCallbacks.add(consumer)
    }

    val molangVars: Object2FloatOpenHashMap<String>?
        get() = ServerModelManager[modelId]?.let { serverModelData ->
            molangStorage.computeIfAbsent(serverModelData.getLoadedModelData().hashId) {
                Object2FloatOpenHashMap(0)
            }
        }

    fun applyFeedback(serverPlayer: ServerPlayer, feedbackData: FeedbackData) {
        val stringValues = feedbackData.stringValues ?: return
        molangStorage.compute(feedbackData.entityId) { _, object2FloatOpenHashMap ->
            if (object2FloatOpenHashMap != null) {
                object2FloatOpenHashMap.putAll(stringValues)
                object2FloatOpenHashMap
            } else {
                Object2FloatOpenHashMap(stringValues)
            }
        }
        animSync2.syncMolangVars(serverPlayer, !dirty, feedbackData.entityId, stringValues)
    }

    fun retainAnimationKeys(intSet: IntSet) {
        val iterator = molangStorage.int2ReferenceEntrySet().fastIterator()
        while (iterator.hasNext()) {
            val entry = iterator.next() as Int2ReferenceMap.Entry<*>
            if (!intSet.contains(entry.intKey)) {
                iterator.remove()
            }
        }
    }

    val animSync: PlayerStateSynchronizer
        get() = animSync2

    fun isDisabled(): Boolean = disabled2

    fun markDirty() {
        dirty = true
    }

    val isDirty: Boolean
        get() = dirty

    fun clearDirty() {
        dirty = false
    }

    fun setMandatory(mandatory: Boolean) {
        if (this.mandatory2 != mandatory) {
            this.mandatory2 = mandatory
            markDirty()
        }
    }

    val isMandatory: Boolean
        get() = mandatory2

    fun serializeNBT(): CompoundTag {
        val compoundTag = CompoundTag()
        compoundTag.putString("model_id", modelId)
        compoundTag.putString("select_texture", selectTexture)
        compoundTag.putBoolean("mandatory", mandatory2)
        compoundTag.putBoolean("disabled", disabled2)
        val compoundTag2 = CompoundTag()
        molangStorage.int2ReferenceEntrySet().fastForEach { entry ->
            val compoundTag3 = CompoundTag()
            entry.value.object2FloatEntrySet().fastForEach { entry2 ->
                compoundTag3.putFloat(entry2.key, entry2.floatValue)
            }
            compoundTag2.put(entry.intKey.toString(), compoundTag3)
        }
        compoundTag.put("molang_storage", compoundTag2)
        return compoundTag
    }

    fun deserializeNBT(compoundTag: CompoundTag) {
        val modelIdStr = compoundTag.getStringOr("model_id", "")
        modelId = modelIdStr
        var selectTextureStr = compoundTag.getStringOr("select_texture", "")
        if (selectTextureStr.length > 4 && selectTextureStr.lowercase().endsWith(".png"))
            selectTextureStr = selectTextureStr.substring(0, selectTextureStr.length - 4)
        setSelectTexture(selectTextureStr)
        mandatory2 = compoundTag.getBooleanOr("mandatory", false)
        disabled2 = compoundTag.getBooleanOr("disabled", false)
        molangStorage.clear()
        val compound = compoundTag.getCompoundOrEmpty("molang_storage")
        for (str in compound.keySet()) {
            runCatching {
                val compound2 = compound.getCompoundOrEmpty(str)
                val i = str.toInt()
                val allKeys = compound2.keySet()
                val object2FloatOpenHashMap = molangStorage.computeIfAbsent(i) {
                    Object2FloatOpenHashMap(allKeys.size)
                }
                for (str2 in allKeys) {
                    object2FloatOpenHashMap.put(str2, compound2.getFloatOr(str2, 0.0f))
                }
            }.onFailure { Constants.LOGGER.error("Failed to deserialize molang storage", it) }
        }
    }

    companion object {
        @JvmStatic
        operator fun get(player: Player): ModelInfoCapability? = ModelInfoCapabilityImpl[player]
    }
}
