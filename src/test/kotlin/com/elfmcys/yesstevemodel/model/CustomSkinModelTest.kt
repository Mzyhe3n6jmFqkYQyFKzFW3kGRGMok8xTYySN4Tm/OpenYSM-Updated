package com.elfmcys.yesstevemodel.model

import com.elfmcys.yesstevemodel.client.ClientModelManager
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

        assertTrue(PlayerSkinTextureManager.isCustomSkinModel("misc/2_steve"))
        assertTrue(ServerModelManager.isCustomSkinModel("misc/2_steve"))
        assertTrue(ClientModelManager.isCustomSkinModel("misc/2_steve"))
        assertEquals(PlayerSkinTextureManager.STEVE_SKIN, PlayerSkinTextureManager.getSkinTexture("misc/2_steve"))

        val alexPath = Paths.get("src/main/resources/assets/yes_steve_model/builtin/misc/1_alex")
        val rawAlex = YSMFolderDeserializer(alexPath).use { it.deserialize() }
        val alexModelInfo = YSMClientMapper.buildModelInfo(rawAlex)

        assertTrue(PlayerSkinTextureManager.isCustomSkinModel("misc/1_alex"))
        assertTrue(ServerModelManager.isCustomSkinModel("misc/1_alex"))
        assertTrue(ClientModelManager.isCustomSkinModel("misc/1_alex"))
        assertEquals(PlayerSkinTextureManager.ALEX_SKIN, PlayerSkinTextureManager.getSkinTexture("misc/1_alex"))

        val defaultPath = Paths.get("src/main/resources/assets/yes_steve_model/builtin/default")
        val rawDefault = YSMFolderDeserializer(defaultPath).use { it.deserialize() }
        val defaultModelInfo = YSMClientMapper.buildModelInfo(rawDefault)
        assertFalse(defaultModelInfo.modelProperties.isCustomSkinModel)
        assertFalse(PlayerSkinTextureManager.isDefaultSkin("default"))
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
        val raw2 = RawYsmModel()
        raw2.properties.sha256 = "0123456789abcdef0123456789abcdef"
        val isCustom2 = snakeCaseObj.has("is_custom_skin_model") && snakeCaseObj.get("is_custom_skin_model").asBoolean
        raw2.properties.isCustomSkinModel = isCustom2
        val info2 = YSMClientMapper.buildModelInfo(raw2)
        assertTrue(info2.modelProperties.isCustomSkinModel)
    }

    @Test
    fun testPlayerSkinTextureManagerDefaults() {
        assertTrue(PlayerSkinTextureManager.isDefaultSkin("misc/2_steve"))
        assertTrue(PlayerSkinTextureManager.isDefaultSkin("misc/1_alex"))
        assertFalse(PlayerSkinTextureManager.isDefaultSkin("misc/3_default_boy"))
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
