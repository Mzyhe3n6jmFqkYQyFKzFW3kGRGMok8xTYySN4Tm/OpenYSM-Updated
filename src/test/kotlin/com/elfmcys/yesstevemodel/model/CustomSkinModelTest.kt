package com.elfmcys.yesstevemodel.model

import com.elfmcys.yesstevemodel.client.event.PlayerSkinTextureManager
import com.elfmcys.yesstevemodel.resource.YSMClientMapper
import com.elfmcys.yesstevemodel.resource.YSMFolderDeserializer
import com.elfmcys.yesstevemodel.resource.pojo.RawYsmModel
import com.google.gson.JsonParser
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CustomSkinModelTest {
    @Test
    fun testBuiltinSteveAndAlexSkinSupportProperties() {
        val stevePath = Paths.get("src/main/resources/assets/yes_steve_model/builtin/misc/2_steve")
        val rawSteve = YSMFolderDeserializer(stevePath).use { it.deserialize() }
        val steveModelInfo = YSMClientMapper.buildModelInfo(rawSteve)

        assertTrue(rawSteve.properties.isCustomSkinModel)
        assertTrue(steveModelInfo.modelProperties.isCustomSkinModel)

        val alexPath = Paths.get("src/main/resources/assets/yes_steve_model/builtin/misc/1_alex")
        val rawAlex = YSMFolderDeserializer(alexPath).use { it.deserialize() }
        val alexModelInfo = YSMClientMapper.buildModelInfo(rawAlex)

        assertTrue(rawAlex.properties.isCustomSkinModel)
        assertTrue(alexModelInfo.modelProperties.isCustomSkinModel)

        val defaultPath = Paths.get("src/main/resources/assets/yes_steve_model/builtin/default")
        val rawDefault = YSMFolderDeserializer(defaultPath).use { it.deserialize() }
        val defaultModelInfo = YSMClientMapper.buildModelInfo(rawDefault)

        assertFalse(rawDefault.properties.isCustomSkinModel)
        assertFalse(defaultModelInfo.modelProperties.isCustomSkinModel)
    }

    @Test
    fun testCustomSkinModelJsonVariants() {
        val snakeCaseJson = """
            {
              "properties": {
                "is_custom_skin_model": true
              }
            }
        """.trimIndent()
        val snakeCaseObj = JsonParser.parseString(snakeCaseJson).asJsonObject.getAsJsonObject("properties")
        val raw1 = RawYsmModel()
        raw1.properties.sha256 = "0123456789abcdef0123456789abcdef"
        raw1.properties.isCustomSkinModel =
            snakeCaseObj.has("is_custom_skin_model") && snakeCaseObj.get("is_custom_skin_model").asBoolean
        val info1 = YSMClientMapper.buildModelInfo(raw1)
        assertTrue(raw1.properties.isCustomSkinModel)
        assertTrue(info1.modelProperties.isCustomSkinModel)

        val camelCaseJson = """
            {
              "properties": {
                "isCustomSkinModel": true
              }
            }
        """.trimIndent()
        val camelCaseObj = JsonParser.parseString(camelCaseJson).asJsonObject.getAsJsonObject("properties")
        val raw2 = RawYsmModel()
        raw2.properties.sha256 = "0123456789abcdef0123456789abcdef"
        raw2.properties.isCustomSkinModel =
            camelCaseObj.has("isCustomSkinModel") && camelCaseObj.get("isCustomSkinModel").asBoolean
        val info2 = YSMClientMapper.buildModelInfo(raw2)
        assertTrue(raw2.properties.isCustomSkinModel)
        assertTrue(info2.modelProperties.isCustomSkinModel)

        val falseJson = """
            {
              "properties": {
                "is_custom_skin_model": false
              }
            }
        """.trimIndent()
        val falseObj = JsonParser.parseString(falseJson).asJsonObject.getAsJsonObject("properties")
        val raw3 = RawYsmModel()
        raw3.properties.sha256 = "0123456789abcdef0123456789abcdef"
        raw3.properties.isCustomSkinModel =
            falseObj.has("is_custom_skin_model") && falseObj.get("is_custom_skin_model").asBoolean
        val info3 = YSMClientMapper.buildModelInfo(raw3)
        assertFalse(raw3.properties.isCustomSkinModel)
        assertFalse(info3.modelProperties.isCustomSkinModel)
    }

    @Test
    fun testPlayerSkinTextureManagerDefaults() {
        assertEquals(PlayerSkinTextureManager.STEVE_SKIN, PlayerSkinTextureManager.getSkinTexture("misc/2_steve"))
        assertEquals(PlayerSkinTextureManager.ALEX_SKIN, PlayerSkinTextureManager.getSkinTexture("misc/1_alex"))
        assertEquals(
            PlayerSkinTextureManager.STEVE_SKIN,
            PlayerSkinTextureManager.getPlayerSkinLocation(null, "misc/2_steve")
        )
        assertEquals(
            PlayerSkinTextureManager.ALEX_SKIN,
            PlayerSkinTextureManager.getPlayerSkinLocation(null, "misc/1_alex")
        )
    }
}
