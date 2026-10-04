package rip.ysm.algorithms

import com.elfmcys.yesstevemodel.client.gui.ModelMetadataPresenter
import com.elfmcys.yesstevemodel.resource.models.ModelPackData
import com.google.gson.JsonParser
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LangTest {
    @Test
    fun testLangFilesJsonSyntax() {
        val langDir = File("src/main/resources/assets/yes_steve_model/lang")
        assertTrue(langDir.exists(), "Lang dir should exist")
        val files = langDir.listFiles() ?: emptyArray()
        for (file in files) {
            if (!file.name.endsWith(".json")) continue
            val bytes = file.readBytes()
            println("[DEBUG_LOG] File: ${file.name}, size: ${bytes.size}")
            val jsonStr = String(bytes, Charsets.UTF_8)
            val json = JsonParser.parseString(jsonStr).asJsonObject
            assertTrue(json.size() > 0, "${file.name} should contain entries")
        }
    }

    @Test
    fun testLocaleMatchingAndFallback() {
        val translations = mapOf(
            "zh_CN" to mapOf(
                "metadata.name" to "博丽灵梦",
                "properties.extra_animation.dance" to "跳舞"
            ),
            "en_US" to mapOf(
                "metadata.name" to "Reimu Hakurei",
                "properties.extra_animation.dance" to "Dance",
                "properties.extra_animation.only_english" to "English Only Action"
            )
        )

        // 1. Direct lowercase match against upper-case key (zh_cn -> zh_CN)
        val zhCnMap = ModelMetadataPresenter.findLocaleMap(translations, "zh_cn")
        assertNotNull(zhCnMap)
        assertEquals("博丽灵梦", zhCnMap["metadata.name"])

        // 2. Hyphenated locale match (zh-CN -> zh_CN)
        val zhHyphenMap = ModelMetadataPresenter.findLocaleMap(translations, "zh-CN")
        assertNotNull(zhHyphenMap)
        assertEquals("博丽灵梦", zhHyphenMap["metadata.name"])

        // 3. Dialect fallback (zh_tw -> zh_CN when zh_tw is not present)
        val zhTwMap = ModelMetadataPresenter.findLocaleMap(translations, "zh_tw")
        assertNotNull(zhTwMap)
        assertEquals("博丽灵梦", zhTwMap["metadata.name"])

        // 4. Default english fallback (ja_jp -> en_US)
        val jaMap = ModelMetadataPresenter.findLocaleMap(translations, "ja_jp")
        assertNotNull(jaMap)
        assertEquals("Reimu Hakurei", jaMap["metadata.name"])

        // 5. Test pack data localization with fallback
        val pack = ModelPackData(
            "path",
            "Default Pack Name",
            "Default Desc",
            null,
            translations
        )
        // With findLocaleMap
        val packZh = ModelMetadataPresenter.findLocaleMap(pack.translations, "zh_cn")
        assertEquals("博丽灵梦", packZh?.get("metadata.name"))
    }
}
