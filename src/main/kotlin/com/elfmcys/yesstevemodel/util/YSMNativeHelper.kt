@file:Suppress("unused")

package com.elfmcys.yesstevemodel.util

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import java.util.*

object YSMNativeHelper {
    @JvmStatic
    fun createTranslatableComponent(str: String, objArr: Array<Any>?): Any {
        if (objArr.isNullOrEmpty()) return Component.translatable(str)
        return Component.translatable(str, *objArr)
    }

    @JvmStatic
    fun createLiteralComponent(str: String?): Any = Component.literal(str ?: StringPool.EMPTY)

    @JvmStatic
    fun appendComponents(obj: Any, obj2: Any): Any = (obj as MutableComponent).append(obj2 as Component)

    @JvmStatic
    fun parseTextureIndices(textureNames: Array<String>): IntArray {
        val indexMap = LinkedHashMap<String, Int>()
        for ((i, name) in textureNames.withIndex()) indexMap["$name.png"] = i
        return indexMap.values.toIntArray()
    }

    @JvmStatic
    @get:Environment(EnvType.CLIENT)
    val clientPlayerUUID: UUID
        get() = Minecraft.getInstance().user.profileId

    @JvmStatic
    val availableCpuCores: Int
        get() = Runtime.getRuntime().availableProcessors()
}