package com.elfmcys.yesstevemodel.client.animation.condition

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Entity
import rip.ysm.compat.touhoulittlemaid.TouhouLittleMaidCompat

class ConditionChair {
    private val idTest: ObjectOpenHashSet<String> = ObjectOpenHashSet()
    private val idPre: String = "chair$"

    fun addTest(name: String) {
        val preSize: Int = idPre.length
        if (name.length <= preSize) {
            return
        }
        val strSubstring: String = name.substring(preSize)
        if (name.startsWith(idPre) && Identifier.tryParse(strSubstring) != null) {
            idTest.add(strSubstring)
        }
    }

    fun doTest(entity: Entity): String {
        val vehicle: Entity? = entity.vehicle
        if (vehicle != null && TouhouLittleMaidCompat.isSimplePlanesEntity(vehicle)) {
            return doIdTest(vehicle)
        }
        return EMPTY
    }

    private fun doIdTest(entity: Entity): String {
        if (idTest.isEmpty()) {
            return EMPTY
        }
        val modelId: String = TouhouLittleMaidCompat.getMaidEntityId(entity)
        if (idTest.contains(modelId)) {
            return idPre + modelId
        }
        return EMPTY
    }

    companion object {
        const val EMPTY: String = ""
    }
}