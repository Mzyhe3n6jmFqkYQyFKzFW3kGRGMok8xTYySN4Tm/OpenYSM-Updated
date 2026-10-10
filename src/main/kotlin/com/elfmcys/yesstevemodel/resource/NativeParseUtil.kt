package com.elfmcys.yesstevemodel.resource

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.resource.pojo.RawYsmModel
import com.ysm.parser.YSMParser
import com.ysm.parser.YSMParserNativeLoader
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

object NativeParseUtil {
    fun parseNative(raw: ByteArray?, modelId: String): RawYsmModel? {
        if (raw == null || raw.isEmpty()) return null
        if (!YSMParserNativeLoader.isAvailable) {
            Constants.LOGGER.warn("Native YSMParser unavailable, skipping model: {}", modelId)
            return null
        }

        var tempDir: Path? = null
        return runCatching {
            val dir = Files.createTempDirectory("ysm_v3_")
            dir.toFile().deleteOnExit()
            tempDir = dir
            val ok = YSMParser.parseBytes(raw, dir.toString())
            if (!ok) {
                Constants.LOGGER.error("YSMParser.parseBytes() failed for model: {}", modelId)
                return null
            }

            val flat = collectFlatFiles(dir)
            if (flat.isEmpty()) {
                Constants.LOGGER.warn("Native parser produced no files for model: {}", modelId)
                return null
            }

            YSMFolderDeserializer(flat).use { deserializer ->
                deserializer.deserialize()
            }
        }.also {
            if (tempDir != null) {
                runCatching {
                    Files.walk(tempDir).use { walk ->
                        walk.sorted(Comparator.reverseOrder()).map { it.toFile() }.forEach { it.delete() }
                    }
                }.onFailure {
                    if (it is IOException) return@onFailure
                    Constants.LOGGER.warn("Failed to delete temporary directory: {}", tempDir, it)
                }
            }
        }.getOrElse {
            Constants.LOGGER.error("Native parse failed for model: {}", modelId, it)
            null
        }
    }

    private fun collectFlatFiles(parsedDir: Path): Map<String, ByteArray> {
        val result = HashMap<String, ByteArray>()
        runCatching {
            Files.walk(parsedDir).use { walk ->
                walk.filter { Files.isRegularFile(it) }.forEach { p ->
                    val rel = parsedDir.relativize(p).toString().replace('\\', '/')
                    runCatching { result[rel] = Files.readAllBytes(p) }
                }
            }
        }.onFailure {
            if (it is IOException) return@onFailure
            Constants.LOGGER.warn("Native parser failed for collect flat files: {}", parsedDir, it)
        }
        return result
    }
}
