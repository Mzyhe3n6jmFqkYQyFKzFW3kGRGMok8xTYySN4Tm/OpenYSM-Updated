package com.elfmcys.yesstevemodel.model

import com.elfmcys.yesstevemodel.client.event.PlayerSkinTextureManager
import com.elfmcys.yesstevemodel.resource.YSMClientMapper
import com.elfmcys.yesstevemodel.resource.YSMFolderDeserializer
import com.elfmcys.yesstevemodel.resource.pojo.RawYsmModel
import com.google.gson.JsonParser
import java.nio.file.Paths
import net.minecraft.resources.Identifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CustomSkinModelTest {
    @Test
    fun testBuiltinSteveAndAlexSkinSupportProperties() {
        val stevePath = Paths.get("src/main/resources/assets/yes_steve_model/builtin/misc/2_steve")
        val rawSteve = YSMFolderDeserializer(stevePath).use { it.deserialize() }
        val steveModelInfo = YSMClientMapper.buildModelInfo(rawSteve)

        assertTrue(rawSteve.properties.isCustomSkinModel)
        assertTrue(steveModelInfo.modelProperties.isCustomSkinModel)
        assertEquals(1, rawSteve.properties.useMcDefaultTexture)
        assertEquals(1, steveModelInfo.modelProperties.useMcDefaultTexture)

        val alexPath = Paths.get("src/main/resources/assets/yes_steve_model/builtin/misc/1_alex")
        val rawAlex = YSMFolderDeserializer(alexPath).use { it.deserialize() }
        val alexModelInfo = YSMClientMapper.buildModelInfo(rawAlex)

        assertTrue(rawAlex.properties.isCustomSkinModel)
        assertTrue(alexModelInfo.modelProperties.isCustomSkinModel)
        assertEquals(2, rawAlex.properties.useMcDefaultTexture)
        assertEquals(2, alexModelInfo.modelProperties.useMcDefaultTexture)

        val defaultPath = Paths.get("src/main/resources/assets/yes_steve_model/builtin/default")
        val rawDefault = YSMFolderDeserializer(defaultPath).use { it.deserialize() }
        val defaultModelInfo = YSMClientMapper.buildModelInfo(rawDefault)

        assertFalse(rawDefault.properties.isCustomSkinModel)
        assertFalse(defaultModelInfo.modelProperties.isCustomSkinModel)
        assertEquals(0, rawDefault.properties.useMcDefaultTexture)
        assertEquals(0, defaultModelInfo.modelProperties.useMcDefaultTexture)
    }

    @Test
    fun testCustomSkinModelJsonVariants() {
        val snakeCaseJson = """
            {
              "properties": {
                "is_custom_skin_model": true,
                "use_mc_default_texture": 1
              }
            }
        """.trimIndent()
        val snakeCaseObj = JsonParser.parseString(snakeCaseJson).asJsonObject.getAsJsonObject("properties")
        val raw1 = RawYsmModel()
        raw1.properties.sha256 = "0123456789abcdef0123456789abcdef"
        raw1.properties.isCustomSkinModel =
            snakeCaseObj.has("is_custom_skin_model") && snakeCaseObj.get("is_custom_skin_model").asBoolean
        raw1.properties.useMcDefaultTexture =
            if (snakeCaseObj.has("use_mc_default_texture")) snakeCaseObj.get("use_mc_default_texture").asInt else 0
        val info1 = YSMClientMapper.buildModelInfo(raw1)
        assertTrue(raw1.properties.isCustomSkinModel)
        assertTrue(info1.modelProperties.isCustomSkinModel)
        assertEquals(1, raw1.properties.useMcDefaultTexture)
        assertEquals(1, info1.modelProperties.useMcDefaultTexture)

        val camelCaseJson = """
            {
              "properties": {
                "isCustomSkinModel": true,
                "useMcDefaultTexture": 2
              }
            }
        """.trimIndent()
        val camelCaseObj = JsonParser.parseString(camelCaseJson).asJsonObject.getAsJsonObject("properties")
        val raw2 = RawYsmModel()
        raw2.properties.sha256 = "0123456789abcdef0123456789abcdef"
        raw2.properties.isCustomSkinModel =
            camelCaseObj.has("isCustomSkinModel") && camelCaseObj.get("isCustomSkinModel").asBoolean
        raw2.properties.useMcDefaultTexture =
            if (camelCaseObj.has("useMcDefaultTexture")) camelCaseObj.get("useMcDefaultTexture").asInt else 0
        val info2 = YSMClientMapper.buildModelInfo(raw2)
        assertTrue(raw2.properties.isCustomSkinModel)
        assertTrue(info2.modelProperties.isCustomSkinModel)
        assertEquals(2, raw2.properties.useMcDefaultTexture)
        assertEquals(2, info2.modelProperties.useMcDefaultTexture)

        val falseJson = """
            {
              "properties": {
                "is_custom_skin_model": false,
                "use_mc_default_texture": 0
              }
            }
        """.trimIndent()
        val falseObj = JsonParser.parseString(falseJson).asJsonObject.getAsJsonObject("properties")
        val raw3 = RawYsmModel()
        raw3.properties.sha256 = "0123456789abcdef0123456789abcdef"
        raw3.properties.isCustomSkinModel =
            falseObj.has("is_custom_skin_model") && falseObj.get("is_custom_skin_model").asBoolean
        raw3.properties.useMcDefaultTexture =
            if (falseObj.has("use_mc_default_texture")) falseObj.get("use_mc_default_texture").asInt else 0
        val info3 = YSMClientMapper.buildModelInfo(raw3)
        assertFalse(raw3.properties.isCustomSkinModel)
        assertFalse(info3.modelProperties.isCustomSkinModel)
        assertEquals(0, raw3.properties.useMcDefaultTexture)
        assertEquals(0, info3.modelProperties.useMcDefaultTexture)
    }

    @Test
    fun testDefaultSkinTextureSelections() {
        assertEquals(PlayerSkinTextureManager.STEVE_SKIN, PlayerSkinTextureManager.getDefaultSkinTexture(1))
        assertEquals(PlayerSkinTextureManager.ALEX_SKIN, PlayerSkinTextureManager.getDefaultSkinTexture(2))
        assertEquals(PlayerSkinTextureManager.STEVE_SKIN, PlayerSkinTextureManager.getDefaultSkinTexture(3))
        assertEquals(PlayerSkinTextureManager.ALEX_SKIN, PlayerSkinTextureManager.getDefaultSkinTexture(4))
        assertNull(PlayerSkinTextureManager.getDefaultSkinTexture(0))
        assertNull(PlayerSkinTextureManager.getDefaultSkinTexture(-1))
        assertNull(PlayerSkinTextureManager.getDefaultSkinTexture(5))

        assertEquals(9, PlayerSkinTextureManager.WIDE_DEFAULT_SKINS.size)
        assertEquals(9, PlayerSkinTextureManager.SLIM_DEFAULT_SKINS.size)
        assertTrue(PlayerSkinTextureManager.WIDE_DEFAULT_SKINS.contains(PlayerSkinTextureManager.STEVE_SKIN))
        assertTrue(PlayerSkinTextureManager.SLIM_DEFAULT_SKINS.contains(PlayerSkinTextureManager.ALEX_SKIN))
        assertTrue(PlayerSkinTextureManager.WIDE_DEFAULT_SKINS.all { it.path.startsWith("textures/entity/player/wide/") })
        assertTrue(PlayerSkinTextureManager.SLIM_DEFAULT_SKINS.all { it.path.startsWith("textures/entity/player/slim/") })
    }

    @Test
    fun testIsDefaultSkinDetection() {
        assertTrue(PlayerSkinTextureManager.isDefaultSkin(PlayerSkinTextureManager.STEVE_SKIN))
        assertTrue(PlayerSkinTextureManager.isDefaultSkin(PlayerSkinTextureManager.ALEX_SKIN))
        assertTrue(PlayerSkinTextureManager.isDefaultSkin(Identifier.parse("minecraft:textures/entity/player/wide/ari.png")))
        assertTrue(PlayerSkinTextureManager.isDefaultSkin(Identifier.parse("minecraft:textures/entity/player/slim/efe.png")))
        assertTrue(PlayerSkinTextureManager.isDefaultSkin(Identifier.parse("minecraft:textures/entity/steve.png")))
        assertTrue(PlayerSkinTextureManager.isDefaultSkin(Identifier.parse("minecraft:textures/entity/alex.png")))

        assertFalse(PlayerSkinTextureManager.isDefaultSkin(Identifier.parse("minecraft:skins/1234567890abcdef")))
        assertFalse(PlayerSkinTextureManager.isDefaultSkin(Identifier.parse("yes_steve_model:textures/custom.png")))
        assertFalse(PlayerSkinTextureManager.isDefaultSkin(null))
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
