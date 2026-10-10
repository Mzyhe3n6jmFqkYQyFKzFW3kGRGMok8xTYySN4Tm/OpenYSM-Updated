package com.elfmcys.yesstevemodel.client.animation.condition

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity

class ConditionVehicle {
    private val idTest: ObjectOpenHashSet<Identifier> = ObjectOpenHashSet()
    private val tagTest: ReferenceArrayList<TagKey<EntityType<*>>> = ReferenceArrayList()
    private val idPre: String = "vehicle$"
    private val tagPre: String = "vehicle#"

    fun addTest(name: String) {
        val preSize = idPre.length
        if (name.length <= preSize) return
        val strSubstring = name.substring(preSize)
        if (name.startsWith(idPre) && Identifier.tryParse(strSubstring) != null) {
            idTest.add(Identifier.parse(strSubstring))
        }
        if (name.startsWith(tagPre) && Identifier.tryParse(strSubstring) != null) {
            tagTest.add(TagKey.create(Registries.ENTITY_TYPE, Identifier.parse(strSubstring)))
        }
    }

    fun doTest(entity: LivingEntity): String {
        val vehicle = entity.vehicle
        if (vehicle == null || !vehicle.isAlive) return EMPTY
        val result = doIdTest(vehicle)
        if (result.isEmpty()) return doTagTest(vehicle)
        return result
    }

    private fun doIdTest(entity: Entity): String {
        if (idTest.isEmpty()) return EMPTY
        val key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.type)
        if (idTest.contains(key)) return idPre + key
        return EMPTY
    }

    private fun doTagTest(entity: Entity): String {
        if (tagTest.isEmpty) return EMPTY
        return tagTest.firstOrNull { entity.type.`is`(it) }?.let { tagPre + it.location() } ?: EMPTY
    }

    companion object {
        const val EMPTY: String = ""
    }
}