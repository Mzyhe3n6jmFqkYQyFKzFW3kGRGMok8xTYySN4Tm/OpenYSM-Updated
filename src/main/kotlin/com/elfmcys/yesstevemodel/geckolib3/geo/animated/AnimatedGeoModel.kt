@file:Suppress("unused")

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

class AnimatedGeoModel(@JvmField val geoModel: GeoModel) {
    private val boneIdsMap: Int2ReferenceMap<IBone>
    private val matrixData: FloatArray
    private val absPivotData: FloatArray
    private val headBones: List<IBone>
    private val leftHandBones: List<IBone>
    private val rightHandBones: List<IBone>
    private val elytraBones: List<IBone>
    private val tacPistolBones: List<IBone>
    private val tacRifleBones: List<IBone>
    private val leftWaistBones: List<IBone>
    private val rightWaistBones: List<IBone>
    private val leftShoulderBones: List<IBone>
    private val rightShoulderBones: List<IBone>
    private val bladeBones: List<IBone>
    private val sheathBones: List<IBone>
    private val backpackBones: List<IBone>
    private val allHeadBone: IBone?
    private val viewLocatorBone: IBone?
    private val leftHandGroupChains: MutableList<List<IBone>> = ReferenceArrayList()
    private val rightHandGroupChains: MutableList<List<IBone>> = ReferenceArrayList()
    private val passengerGroupChains: MutableList<List<IBone>> = ReferenceArrayList()

    @JvmField
    @PublishedApi
    internal var rawTouhouMaidData: Any? = null

    init {
        val bones = geoModel.topLevelBones()
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
        geoModel.extraLeftHandGroups.forEach { intList -> leftHandGroupChains.add(lookupBones(intList)) }
        geoModel.extraRightHandGroups.forEach { intList2 -> rightHandGroupChains.add(lookupBones(intList2)) }
        geoModel.passengerGroups.forEach { intList3 -> passengerGroupChains.add(lookupBones(intList3)) }
    }

    private fun lookupBones(intList: IntList): List<IBone> {
        val referenceArrayList = ReferenceArrayList<IBone>(intList.size)
        intList.forEach { i -> referenceArrayList.add(boneIdsMap.get(i)) }
        return ReferenceLists.unmodifiable(referenceArrayList)
    }

    fun getMatrixData(): FloatArray = matrixData

    fun getAbsPivotData(): FloatArray = absPivotData

    fun bones(): Int2ReferenceMap<IBone> = boneIdsMap

    fun getGeoModel(): GeoModel = geoModel

    fun leftHandBones(): List<IBone> = leftHandBones

    fun rightHandChain(): List<List<IBone>> = leftHandGroupChains

    fun rightHandBones(): List<IBone> = rightHandBones

    fun leftHandChains(): List<List<IBone>> = rightHandGroupChains

    fun passengerGroupChains(): List<List<IBone>> = passengerGroupChains

    fun elytraBones(): List<IBone> = elytraBones

    fun backpackBones(): List<IBone> = backpackBones

    fun tacPistolBones(): List<IBone> = tacPistolBones

    fun tacRifleBones(): List<IBone> = tacRifleBones

    fun leftWaistBones(): List<IBone> = leftWaistBones

    fun rightWaistBones(): List<IBone> = rightWaistBones

    fun leftShoulderBones(): List<IBone> = leftShoulderBones

    fun rightShoulderBones(): List<IBone> = rightShoulderBones

    fun bladeBones(): List<IBone> = bladeBones

    fun sheathBones(): List<IBone> = sheathBones

    fun allHeadBone(): IBone? = allHeadBone

    fun viewLocatorBone(): IBone? = viewLocatorBone

    fun headBones(): List<IBone> = headBones

    inline fun <reified T> getTouhouMaidData(): T? {
        if (rawTouhouMaidData == null) {
            rawTouhouMaidData = TouhouMaidBoneProcessor.createLocationModel(this)
        }
        return rawTouhouMaidData as? T
    }

    companion object {
        const val MATRIX_STRIDE: Int = 12
        const val ABS_PIVOT_DATA_STRIDE: Int = 4
        val ALL_HEAD_ID: Int = StringPool.computeIfAbsent("AllHead")
        val VIEW_LOCATOR_ID: Int = StringPool.computeIfAbsent("ViewLocator")
    }
}