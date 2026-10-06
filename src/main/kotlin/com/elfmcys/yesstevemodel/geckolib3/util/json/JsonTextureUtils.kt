@file:Suppress("unused")

package com.elfmcys.yesstevemodel.geckolib3.util.json

import com.elfmcys.yesstevemodel.client.texture.OuterFileTexture
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.apache.commons.compress.utils.Lists
import org.apache.commons.lang3.tuple.Pair
import rip.ysm.compat.oculus.ShadersTextureType

/**
 * error.yes_steve_model.decode_texture
 * 加载失败字段
 */
object JsonTextureUtils {
    /**
     * 给player用的
     */
    @JvmStatic
    fun getTextures(
        resource: Map<String, ByteArray>,
        element: JsonElement?
    ): OrderedStringMap<String, OuterFileTexture>? {
        if (element == null || !element.isJsonArray) return null

        val keys: MutableList<String> = Lists.newArrayList()
        val values: MutableList<OuterFileTexture> = Lists.newArrayList()

        for (rawTexture in element.asJsonArray) {
            val texture: Pair<String, OuterFileTexture> = getTexture(resource, rawTexture) ?: continue
            keys.add(texture.key)
            values.add(texture.value)
        }

        return OrderedStringMap(keys.toTypedArray(), values.toTypedArray())
    }

    @JvmStatic
    fun getTexture(resource: Map<String, ByteArray>, element: JsonElement?): Pair<String, OuterFileTexture>? {
        if (element == null) return null

        when {
            element.isJsonObject -> {
                val jsonObj: JsonObject = element.asJsonObject
                if (jsonObj.has("uv")) {
                    val uvPath = jsonObj.get("uv").asString
                    val bytes = resource[uvPath] ?: return null
                    val name = extractTextureName(uvPath)
                    val texture = OuterFileTexture(bytes)
                    val fbo: MutableMap<ShadersTextureType, OuterFileTexture> = HashMap()
                    if (jsonObj.has("normal")) {
                        val normalPath = jsonObj.get("normal").asString
                        resource[normalPath]?.let { fbo[ShadersTextureType.NORMAL] = OuterFileTexture(it) }
                    }
                    if (jsonObj.has("specular")) {
                        val specularPath = jsonObj.get("specular").asString
                        resource[specularPath]?.let { fbo[ShadersTextureType.SPECULAR] = OuterFileTexture(it) }
                    }
                    texture.setSuffixTextures(fbo)
                    return Pair.of(name, texture)
                }
            }

            element.isJsonPrimitive && element.asJsonPrimitive.isString -> {
                val texPath = element.asString
                val bytes = resource[texPath] ?: return null
                val name = extractTextureName(texPath)
                return Pair.of(name, OuterFileTexture(bytes))
            }
        }

        return null
    }

    @JvmStatic
    private fun extractTextureName(path: String): String {
        val lastSlash = path.lastIndexOf('/')
        val lastDot = path.lastIndexOf('.')
        if (lastDot <= lastSlash || lastDot == -1) return path.substring(lastSlash + 1)
        return path.substring(lastSlash + 1, lastDot)
    }
}