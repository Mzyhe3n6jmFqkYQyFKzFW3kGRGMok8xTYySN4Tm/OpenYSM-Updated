@file:Suppress("unused")

package com.elfmcys.yesstevemodel.capability

import com.elfmcys.yesstevemodel.capability.fabric.PlayerCapabilityImpl
import com.elfmcys.yesstevemodel.client.animation.molang.struct.RoamingStruct
import com.elfmcys.yesstevemodel.client.animation.molang.struct.RoamingSyncBatch
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable
import com.elfmcys.yesstevemodel.client.entity.PlayerEntityFrameState
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel
import com.elfmcys.yesstevemodel.molang.runtime.Int2FloatOpenHashMapStruct
import com.elfmcys.yesstevemodel.molang.runtime.Struct
import com.elfmcys.yesstevemodel.network.NetworkHandler
import com.elfmcys.yesstevemodel.network.message.C2SCompleteFeedbackPacket
import com.elfmcys.yesstevemodel.network.message.FeedbackData
import it.unimi.dsi.fastutil.ints.Int2FloatMap
import it.unimi.dsi.fastutil.ints.Int2FloatMaps
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.player.LocalPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import rip.ysm.compat.bettercombat.BetterCombatCompat
import rip.ysm.compat.firstperson.FirstPersonCompat

@Environment(EnvType.CLIENT)
class PlayerCapability(player: Player) : CustomPlayerEntity(player, player is LocalPlayer, true) {
    private val molangVarsMap: Int2ReferenceOpenHashMap<MolangVarHolder> = Int2ReferenceOpenHashMap(8)
    private var currentModelHashId: Int = 0
    private var serverVarContainer2: Struct? = null

    override fun createPositionTracker(entity: Player): PlayerEntityFrameState =
        PlayerEntityFrameState(entity, entity is LocalPlayer)

    override val positionTracker: PlayerEntityFrameState
        get() = super.positionTracker as PlayerEntityFrameState

    override val serverVarContainer: Struct?
        get() = serverVarContainer2

    override fun onModelLoaded(modelAssembly: ModelAssembly) {
        super.onModelLoaded(modelAssembly)
        currentModelHashId = modelAssembly.modelData.hashId
    }

    override fun clearModel() {
        currentModelHashId = 0
        super.clearModel()
    }

    override var currentModel: AnimatedGeoModel?
        get() = super.currentModel
        set(value) {
            super.currentModel = value
            val varHolder = molangVarsMap.get(currentModelHashId)
            varHolder?.currentVars?.let {
                if (isLocalPlayerModel) {
                    serverVarContainer2 = RoamingStruct(currentModelHashId, it)
                    return
                } else {
                    serverVarContainer2 = Int2FloatOpenHashMapStruct(it)
                    return
                }
            }
            serverVarContainer2 = null
        }

    override fun reset() {
        serverVarContainer2 = null
        super.reset()
    }

    override fun applyHeadTracking(event: AnimationEvent<AnimatableEntity<Player>>, z: Boolean) {
        super.applyHeadTracking(event, z)
        val model2 = currentModel
        if (model2 != null && isLocalPlayerModel && !event.isFirstPerson && FirstPersonCompat.isModLoaded) {
            if (model2.allHeadBone() != null) {
                model2.allHeadBone()?.setHidden(FirstPersonCompat.shouldHideHead())
            }
            when {
                model2.viewLocatorBone() != null -> {
                    FirstPersonCompat.setCameraDistance(
                        (model2.viewLocatorBone() ?: return).pivotY * widthScale
                    )
                }

                z && model2.headBones().isNotEmpty() -> {
                    val bone = model2.headBones()[model2.headBones().size - 1]
                    FirstPersonCompat.setCameraDistance(bone.pivotY * widthScale)
                }
            }
        }
    }

    override fun resetHeadTracking(wasAnimEvaluated: Boolean) {
        super.resetHeadTracking(wasAnimEvaluated)
        val model2 = currentModel
        if (model2 != null && isLocalPlayerModel) {
            if ((FirstPersonCompat.isModLoaded || BetterCombatCompat.isModLoaded) && model2.allHeadBone() != null) {
                model2.allHeadBone()?.setHidden(false)
            }
        }
    }

    fun updateMolangVars(i: Int, int2FloatOpenHashMap: Int2FloatOpenHashMap) {
        val varHolder = molangVarsMap.computeIfAbsent(i) { MolangVarHolder() }
        if (isLocalPlayerModel) {
            if (varHolder.currentVars == null || serverVarContainer2 == null) {
                varHolder.currentVars = int2FloatOpenHashMap
                varHolder.applyPendingDeltas()
                if (i == currentModelHashId) {
                    serverVarContainer2 = RoamingStruct(i, int2FloatOpenHashMap)
                    clearAnimationControllers()
                    return
                }
                return
            }
            return
        }
        varHolder.currentVars = int2FloatOpenHashMap
        varHolder.applyPendingDeltas()
        if (i == currentModelHashId) {
            serverVarContainer2 = Int2FloatOpenHashMapStruct(int2FloatOpenHashMap)
        }
    }

    fun hasMolangVars(i: Int): Boolean = molangVarsMap.containsKey(i)

    private fun applyMolangDelta(i: Int, int2FloatMap: Int2FloatMap) {
        val vehicle = entity.vehicle
        if (i == currentModelHashId && vehicle != null && vehicle.firstPassenger == entity) {
            VehicleCapability[vehicle]?.updateFloatMap(int2FloatMap)
        }
    }

    fun enqueueMolangDelta(i: Int, int2FloatMap: Int2FloatMap) {
        if (!isLocalPlayerModel && int2FloatMap.isNotEmpty()) {
            val varHolder = molangVarsMap.computeIfAbsent(i) { MolangVarHolder() }
            if (varHolder.currentVars != null) {
                varHolder.currentVars!!.putAll(int2FloatMap)
            } else {
                varHolder.pendingDeltas.enqueue(int2FloatMap)
            }
            applyMolangDelta(i, int2FloatMap)
        }
    }

    fun tickAnimations() {
        if (isLocalPlayerModel && currentModelHashId != 0) {
            val struct = serverVarContainer2
            if (struct is RoamingStruct && struct.hasPendingChanges()) {
                val syncBatch: RoamingSyncBatch = struct.consumePendingBoneData()
                applyMolangDelta(syncBatch.modelHashId(), syncBatch.changedVariables())
                val size = syncBatch.changedVariables().size
                val strArr = arrayOfNulls<String>(size)
                val fArr = FloatArray(size)
                var i = 0
                val it = Int2FloatMaps.fastIterable(syncBatch.changedVariables()).iterator()
                while (it.hasNext()) {
                    val entry = it.next() as Int2FloatMap.Entry
                    val str = StringPool.getString(entry.intKey)
                    if (str.length <= RoamingStruct.MAX_VAR_NAME_LENGTH) {
                        strArr[i] = str
                        fArr[i] = entry.floatValue
                    } else {
                        strArr[i] = StringPool.EMPTY
                        fArr[i] = 0.0f
                    }
                    i++
                }

                NetworkHandler.sendToServer(
                    C2SCompleteFeedbackPacket(
                        FeedbackData(
                            currentModelHashId,
                            Object2FloatArrayMap(strArr, fArr),
                            null,
                            entity.id
                        )
                    )
                )
            }
        }
    }

    fun copyFrom(playerCapability: PlayerCapability) {
        molangVarsMap.clear()
        for (entry in playerCapability.molangVarsMap.int2ReferenceEntrySet()) {
            val holder = MolangVarHolder()
            entry.value.currentVars?.let { holder.currentVars = Int2FloatOpenHashMap(it) }
            molangVarsMap.put(entry.intKey, holder)
        }
        initModelWithTexture(playerCapability.modelId, playerCapability.currentTextureName)
        setForceDisabled(playerCapability.isForceDisabled)
        val holder = molangVarsMap[currentModelHashId]
        val vars = holder?.currentVars
        if (vars != null) {
            serverVarContainer2 =
                if (isLocalPlayerModel) RoamingStruct(currentModelHashId, vars) else Int2FloatOpenHashMapStruct(vars)
        }
    }

    override fun buildRenderShape(
        modelAssembly: ModelAssembly,
        isDefault: Boolean
    ): LivingAnimatable<Player>.TexturedModelWrapper {
        return TexturedModelWrapper(modelAssembly, isDefault, true, true, 600)
    }

    private class MolangVarHolder {
        @Volatile
        var currentVars: Int2FloatOpenHashMap? = null
        val pendingDeltas: ObjectArrayFIFOQueue<Int2FloatMap> = ObjectArrayFIFOQueue(4)

        fun applyPendingDeltas() {
            while (!pendingDeltas.isEmpty) currentVars?.putAll(pendingDeltas.dequeue())
        }
    }

    companion object {
        @JvmStatic
        operator fun get(player: Player): PlayerCapability? = PlayerCapabilityImpl[player]

        @JvmStatic
        operator fun get(entity: Entity): PlayerCapability? = PlayerCapabilityImpl[entity]
    }
}
