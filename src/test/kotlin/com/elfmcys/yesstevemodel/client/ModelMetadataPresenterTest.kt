package com.elfmcys.yesstevemodel.client

import com.elfmcys.yesstevemodel.client.gui.ModelMetadataPresenter
import com.elfmcys.yesstevemodel.resource.models.ModelPackData
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.File
import java.nio.charset.StandardCharsets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ModelMetadataPresenterTest {
    @Test
    fun testFindLocaleMapNoZhTwToZhCnFallback() {
        val translations = mapOf(
            "zh_cn" to mapOf("name" to "酒狐与小伙伴", "description" to "可爱酒狐和她的小伙伴们")
        )

        // zh_cn should find zh_cn
        val zhCnMap = ModelMetadataPresenter.findLocaleMap(translations, "zh_cn")
        assertEquals("酒狐与小伙伴", zhCnMap?.get("name"))

        // zh_tw should NOT match zh_cn!
        val zhTwMap = ModelMetadataPresenter.findLocaleMap(translations, "zh_tw")
        assertNull(zhTwMap, "zh_tw should not match zh_cn")

        // ja_jp should NOT match zh_cn!
        val jaJpMap = ModelMetadataPresenter.findLocaleMap(translations, "ja_jp")
        assertNull(jaJpMap, "ja_jp should not match zh_cn")
    }

    @Test
    fun testLookupTranslationFallback() {
        val translations = mapOf(
            "zh_cn" to mapOf("name" to "酒狐与小伙伴", "description" to "可爱酒狐和她的小伙伴们")
        )
        val packData = ModelPackData(
            path = "wine_fox",
            name = "Wine Fox & Friends",
            description = "Cute Wine Fox and her friends",
            texture = null,
            translations = translations
        )

        // When requesting zh_tw without zh_tw translations, should fall back to default name
        val resultZhTw = ModelMetadataPresenter.lookupTranslation(translations, "zh_tw", "name", packData.name)
        assertEquals("Wine Fox & Friends", resultZhTw)

        // When requesting zh_cn, should return zh_cn translation
        val resultZhCn = ModelMetadataPresenter.lookupTranslation(translations, "zh_cn", "name", packData.name)
        assertEquals("酒狐与小伙伴", resultZhCn)

        // When requesting ja_jp without ja_jp translations, should fall back to default name
        val resultJaJp = ModelMetadataPresenter.lookupTranslation(translations, "ja_jp", "name", packData.name)
        assertEquals("Wine Fox & Friends", resultJaJp)
    }

    @Test
    fun testInspectMissingLangKeys() {
        val langDir = File("src/main/resources/assets/yes_steve_model/lang")
        val gson = Gson()
        val enUsFile = File(langDir, "en_us.json")
        val enUsJson = gson.fromJson(enUsFile.readText(StandardCharsets.UTF_8), JsonObject::class.java)
        val enUsKeys = enUsJson.keySet()

        val langFiles =
            langDir.listFiles { _, name -> name.endsWith(".json") && name != "en_us.json" }?.sortedBy { it.name }
                ?: emptyList()
        val allMissing = mutableMapOf<String, List<String>>()
        for (langFile in langFiles) {
            val json = gson.fromJson(langFile.readText(StandardCharsets.UTF_8), JsonObject::class.java)
            val missingKeys = enUsKeys.filter { !json.has(it) }
            if (missingKeys.isNotEmpty()) {
                allMissing[langFile.name] = missingKeys
            }
        }
        assertEquals(
            emptyMap<String, List<String>>(),
            allMissing,
            "All language files should have all keys from en_us.json"
        )
    }

    @Test
    fun testBuiltinDefaultLangFiles() {
        val langDir = File("src/main/resources/assets/yes_steve_model/builtin/default/lang")
        val gson = Gson()
        val enUsFile = File(langDir, "en_us.json")
        val enUsJson = gson.fromJson(enUsFile.readText(StandardCharsets.UTF_8), JsonObject::class.java)
        val enUsKeys = enUsJson.keySet()

        val expectedLocales = listOf(
            "en_us", "es_es", "fr_fr", "id_id", "ja_jp", "ko_kr",
            "pt_br", "ru_ru", "tr_tr", "uk_ua", "vi_vn", "zh_cn", "zh_tw"
        )

        for (locale in expectedLocales) {
            val file = File(langDir, "$locale.json")
            assertEquals(true, file.exists(), "Language file $locale.json must exist in default/lang")
            val json = gson.fromJson(file.readText(StandardCharsets.UTF_8), JsonObject::class.java)
            val missing = enUsKeys.filter { !json.has(it) }
            assertEquals(emptyList<String>(), missing, "default/lang/$locale.json missing keys: $missing")
        }
    }

    @Test
    fun testBuiltinPackJsonLangKeys() {
        val gson = Gson()
        val packPaths = listOf(
            "src/main/resources/assets/yes_steve_model/builtin/misc/ysm-pack.json",
            "src/main/resources/assets/yes_steve_model/builtin/wine_fox/ysm-pack.json"
        )
        val expectedLocales = listOf(
            "en_us", "es_es", "fr_fr", "id_id", "ja_jp", "ko_kr",
            "pt_br", "ru_ru", "tr_tr", "uk_ua", "vi_vn", "zh_cn", "zh_tw"
        )

        for (packPath in packPaths) {
            val file = File(packPath)
            assertEquals(true, file.exists(), "Pack file $packPath must exist")
            val json = gson.fromJson(file.readText(StandardCharsets.UTF_8), JsonObject::class.java)
            val langObj = json.getAsJsonObject("lang")
            assertEquals(true, langObj != null, "Pack $packPath must have 'lang' object")
            for (locale in expectedLocales) {
                val localeObj = langObj.getAsJsonObject(locale)
                assertEquals(true, localeObj != null, "Pack $packPath missing locale '$locale'")
                assertEquals(true, localeObj.has("name"), "Pack $packPath locale '$locale' missing 'name'")
                assertEquals(
                    true,
                    localeObj.has("description"),
                    "Pack $packPath locale '$locale' missing 'description'"
                )
            }
        }
    }
}
