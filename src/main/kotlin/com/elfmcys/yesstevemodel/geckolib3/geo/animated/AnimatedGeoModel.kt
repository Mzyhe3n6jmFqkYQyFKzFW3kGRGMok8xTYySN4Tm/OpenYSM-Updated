@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.elfmcys.yesstevemodel.geckolib3.geo.animated

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap
import it.unimi.dsi.fastutil.ints.Int2ReferenceMaps
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap
import it.unimi.dsi.fastutil.ints.IntList
import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import it.unimi.dsi.fastutil.objects.ReferenceLists
import rip.ysm.compat.touhoulittlemaid.TouhouMaidBoneProcessor

class AnimatedGeoModel(val geoModel: GeoModel) {
    private val boneIdsMap: Int2ReferenceMap<IBone>
    val matrixData: FloatArray
    val absPivotData: FloatArray
    val headBones: List<IBone>
    val leftHandBones: List<IBone>
    val rightHandBones: List<IBone>
    val elytraBones: List<IBone>
    val tacPistolBones: List<IBone>
    val tacRifleBones: List<IBone>
    val leftWaistBones: List<IBone>
    val rightWaistBones: List<IBone>
    val leftShoulderBones: List<IBone>
    val rightShoulderBones: List<IBone>
    val bladeBones: List<IBone>
    val sheathBones: List<IBone>
    val backpackBones: List<IBone>
    val allHeadBone: IBone?
    val viewLocatorBone: IBone?
    val rightHandChain: List<List<IBone>>
        field: MutableList<List<IBone>> = ReferenceArrayList()
    val leftHandChains: List<List<IBone>>
        field: MutableList<List<IBone>> = ReferenceArrayList()
    val passengerGroupChains: MutableList<List<IBone>> = ReferenceArrayList()

    @JvmField
    @PublishedApi
    internal var rawTouhouMaidData: Any? = null

    init {
        val bones = geoModel.topLevelBones
        matrixData = FloatArray(MATRIX_STRIDE * bones.size)
        absPivotData = FloatArray(ABS_PIVOT_DATA_STRIDE * bones.size)
        val map = Int2ReferenceOpenHashMap<IBone>(bones.size)
        for (i in bones.indices) {
            val renderConfig = bones[i]
            map.put(
                renderConfig.boneId,
                AnimatedGeoBone(renderConfig, matrixData, i * MATRIX_STRIDE, absPivotData, i * ABS_PIVOT_DATA_STRIDE)
            )
        }
        boneIdsMap = Int2ReferenceMaps.unmodifiable(map)
        headBones = lookupBones(geoModel.headIds)
        leftHandBones = lookupBones(geoModel.leftHandIds)
        rightHandBones = lookupBones(geoModel.rightHandIds)
        elytraBones = lookupBones(geoModel.elytraIds)
        tacPistolBones = lookupBones(geoModel.tacPistolIds)
        tacRifleBones = lookupBones(geoModel.tacRifleIds)
        leftWaistBones = lookupBones(geoModel.leftWaistIds)
        rightWaistBones = lookupBones(geoModel.rightWaistIds)
        leftShoulderBones = lookupBones(geoModel.leftShoulderIds)
        rightShoulderBones = lookupBones(geoModel.rightShoulderIds)
        bladeBones = lookupBones(geoModel.bladeIds)
        sheathBones = lookupBones(geoModel.sheathIds)
        backpackBones = lookupBones(geoModel.backpackIds)
        allHeadBone = map.get(ALL_HEAD_ID)
        viewLocatorBone = map.get(VIEW_LOCATOR_ID)
        geoModel.extraLeftHandGroups.forEach { intList -> rightHandChain.add(lookupBones(intList)) }
        geoModel.extraRightHandGroups.forEach { intList2 -> leftHandChains.add(lookupBones(intList2)) }
        geoModel.passengerGroups.forEach { intList3 -> passengerGroupChains.add(lookupBones(intList3)) }
    }

    private fun lookupBones(intList: IntList): List<IBone> {
        val referenceArrayList = ReferenceArrayList<IBone>(intList.size)
        intList.forEach { i -> referenceArrayList.add(boneIdsMap.get(i)) }
        return ReferenceLists.unmodifiable(referenceArrayList)
    }

    val bones: Int2ReferenceMap<IBone>
        get() = boneIdsMap

    inline fun <reified T> getTouhouMaidData(): T? {
        if (rawTouhouMaidData == null) rawTouhouMaidData = TouhouMaidBoneProcessor.createLocationModel(this)
        return rawTouhouMaidData as? T
    }

    companion object {
        const val MATRIX_STRIDE: Int = 12
        const val ABS_PIVOT_DATA_STRIDE: Int = 4
        val ALL_HEAD_ID: Int = StringPool.computeIfAbsent("AllHead")
        val VIEW_LOCATOR_ID: Int = StringPool.computeIfAbsent("ViewLocator")
    }
}