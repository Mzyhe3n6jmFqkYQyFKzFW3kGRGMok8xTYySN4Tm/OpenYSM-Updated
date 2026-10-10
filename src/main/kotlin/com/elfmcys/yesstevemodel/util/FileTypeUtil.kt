package com.elfmcys.yesstevemodel.util

import com.elfmcys.yesstevemodel.NameSpaces
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import it.unimi.dsi.fastutil.Pair
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.EntityType
import java.util.*

object FileTypeUtil {
    const val DEFAULT_MODEL_ID: String = "default"
    const val DEFAULT_TEXTURE: String = "default"
    private val ARCHIVE_EXTENSIONS: Set<String> = setOf(".zip", ".7z", ".ysm")

    fun parseHexId(str: String): Int = Integer.parseUnsignedInt(str.substring(0, 8), 16)

    fun splitFileNameAndParentDir(filePath: String): Pair<String, String> {
        val lastSlashIndex = filePath.lastIndexOf('/')
        if (lastSlashIndex == -1) {
            return Pair.of(filePath, StringPool.EMPTY)
        }
        return Pair.of(filePath.substring(lastSlashIndex + 1), filePath.substring(0, lastSlashIndex + 1))
    }

    fun getNameWithoutArchiveExtension(filePath: String): String {
        val fileName = filePath.substringAfterLast('/')
        val dotIndex = fileName.lastIndexOf('.')
        if (dotIndex < 1 || fileName.substring(dotIndex).lowercase(Locale.US) !in ARCHIVE_EXTENSIONS) {
            return fileName
        }
        return fileName.substring(0, dotIndex)
    }

    fun getFinalPathSegment(path: String?): String {
        if (path.isNullOrEmpty()) {
            return StringPool.EMPTY
        }
        val trimmedPath = if (path.endsWith('/')) path.dropLast(1) else path
        return trimmedPath.substringAfterLast('/')
    }

    fun getPackIconLocation(str: String): Identifier {
        return NameSpaces.MOD.path("model_pack_icon/${str.hashCode()}")
    }

    fun resolveEntityTypes(strArr: Array<String>): Set<Identifier> {
        val hashSet = HashSet<Identifier>()
        for (str in strArr) {
            if (str.startsWith("#")) {
                val identifier = Identifier.tryParse(str.substring(1)) ?: continue
                val tagKey: TagKey<EntityType<*>> = TagKey.create(Registries.ENTITY_TYPE, identifier)
                BuiltInRegistries.ENTITY_TYPE.get(tagKey).ifPresent { holderSet ->
                    for (holder in holderSet) {
                        holder.unwrapKey().ifPresent { rk -> hashSet.add(rk.identifier()) }
                    }
                }
            } else {
                val identifier = Identifier.tryParse(str)
                if (identifier != null) {
                    hashSet.add(identifier)
                }
            }
        }
        return hashSet
    }
}