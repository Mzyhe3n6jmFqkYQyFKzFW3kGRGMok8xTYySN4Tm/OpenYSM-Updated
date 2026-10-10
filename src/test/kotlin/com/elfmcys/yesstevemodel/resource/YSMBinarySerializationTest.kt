package com.elfmcys.yesstevemodel.resource

import com.elfmcys.yesstevemodel.resource.pojo.RawYsmModel
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class YSMBinarySerializationTest {

    @Test
    fun testFormat32SerializationAndDeserialization() {
        val model = RawYsmModel().apply {
            formatVersion = 32
            metadata.name = "Test Model"
            properties.sha256 = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
            footer.version = 32
            footer.unkInt1 = 1
            footer.rand = "test_rand_str"
            footer.time = 1775738769L
            footer.extra = "test_extra"
            footer.unkInt2 = 0

            val geo = RawYsmModel.RawGeometry().apply {
                modelType = 1
                identifier = "geometry.test"
                sha256 = "geom_hash"
                textureWidth = 64f
                textureHeight = 64f
            }
            mainEntity.mainModel = geo

            val tex = RawYsmModel.RawTexture().apply {
                name = "default.png"
                width = 64
                height = 64
                imageFormat = 2 // PNG
                data = byteArrayOf(0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte(), 0x0D, 0x0A, 0x1A, 0x0A)
            }
            mainEntity.textures["default.png"] = tex
        }

        val serializedBuf = YSMBinarySerializer.serialize(model, 32, true)
        val data = serializedBuf.toArray()

        YSMBinaryDeserializer(data, 32).use { deserializer ->
            val deserializedModel = deserializer.deserializeKeepOpen()
            deserializer.parseYSMFooter(deserializedModel)

            assertEquals(32, deserializedModel.formatVersion)
            assertEquals("Test Model", deserializedModel.metadata.name)
            assertNotNull(deserializedModel.mainEntity.mainModel)
            assertEquals("geometry.test", deserializedModel.mainEntity.mainModel?.identifier)
            assertEquals(1, deserializedModel.mainEntity.textures.size)
            assertNotNull(deserializedModel.mainEntity.textures["default.png"])

            assertEquals(32, deserializedModel.footer.version)
            assertEquals(1, deserializedModel.footer.unkInt1)
            assertEquals("test_rand_str", deserializedModel.footer.rand)
            assertEquals(1775738769L, deserializedModel.footer.time)
            assertEquals("test_extra", deserializedModel.footer.extra)
            assertEquals(0, deserializedModel.footer.unkInt2)
        }
    }

    @Test
    fun testFormat40SerializationAndDeserialization() {
        val model = RawYsmModel().apply {
            formatVersion = 40
            metadata.name = "Test Model 40"
            properties.sha256 = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
            properties.isCustomSkinModel = true
            properties.useMcDefaultTexture = 2
            footer.version = 40
            footer.unkInt1 = 1
            footer.rand = "test_rand_40"
            footer.time = 1775738769L
            footer.extra = "test_extra_40"
            footer.unkInt2 = 0

            val geo = RawYsmModel.RawGeometry().apply {
                modelType = 1
                identifier = "geometry.test40"
                sha256 = "geom_hash40"
                textureWidth = 64f
                textureHeight = 64f
            }
            mainEntity.mainModel = geo

            val tex = RawYsmModel.RawTexture().apply {
                name = "default.png"
                width = 64
                height = 64
                imageFormat = 2 // PNG
                data = byteArrayOf(0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte(), 0x0D, 0x0A, 0x1A, 0x0A)
            }
            mainEntity.textures["default.png"] = tex
        }

        val serializedBuf = YSMBinarySerializer.serialize(model, 40, true)
        val data = serializedBuf.toArray()

        YSMBinaryDeserializer(data, 40).use { deserializer ->
            val deserializedModel = deserializer.deserializeKeepOpen()
            deserializer.parseYSMFooter(deserializedModel)

            assertEquals(40, deserializedModel.formatVersion)
            assertEquals("Test Model 40", deserializedModel.metadata.name)
            assertNotNull(deserializedModel.mainEntity.mainModel)
            assertEquals("geometry.test40", deserializedModel.mainEntity.mainModel?.identifier)
            assertEquals(1, deserializedModel.mainEntity.textures.size)
            assertNotNull(deserializedModel.mainEntity.textures["default.png"])
            assert(deserializedModel.properties.isCustomSkinModel)
            assertEquals(2, deserializedModel.properties.useMcDefaultTexture)

            assertEquals(40, deserializedModel.footer.version)
            assertEquals(1, deserializedModel.footer.unkInt1)
            assertEquals("test_rand_40", deserializedModel.footer.rand)
            assertEquals(1775738769L, deserializedModel.footer.time)
            assertEquals("test_extra_40", deserializedModel.footer.extra)
            assertEquals(0, deserializedModel.footer.unkInt2)
        }
    }

    @Test
    fun testFooterWithUnkInt1Zero() {
        val model = RawYsmModel().apply {
            formatVersion = 40
            footer.version = 65535
            footer.unkInt1 = 0
            footer.time = 0L
        }

        val serializedBuf = YSMBinarySerializer.serialize(model, 40, true)
        val data = serializedBuf.toArray()

        YSMBinaryDeserializer(data, 40).use { deserializer ->
            val deserializedModel = deserializer.deserializeKeepOpen()
            deserializer.parseYSMFooter(deserializedModel)

            assertEquals(65535, deserializedModel.footer.version)
            assertEquals(0, deserializedModel.footer.unkInt1)
            assertEquals(0L, deserializedModel.footer.time)
        }
    }

    @Test
    fun testBuiltinDefaultModelSerializationCycle() {
        val defaultPath = Paths.get("src/main/resources/assets/yes_steve_model/builtin/default")
        val rawModel = YSMFolderDeserializer(defaultPath).use { it.deserialize() }
        rawModel.footer.version = 40
        rawModel.footer.unkInt1 = 1
        rawModel.footer.rand = "random1234"
        rawModel.footer.time = 1234567890L
        rawModel.footer.extra = "OpenYSM"
        rawModel.footer.unkInt2 = 0

        val serializedBuf = YSMBinarySerializer.serialize(rawModel, 40, true)
        val data = serializedBuf.toArray()

        YSMBinaryDeserializer(data, 40).use { deserializer ->
            val deserializedModel = deserializer.deserializeKeepOpen()
            deserializer.parseYSMFooter(deserializedModel)

            assertEquals(40, deserializedModel.formatVersion)
            assertEquals("Default", deserializedModel.metadata.name)
            assertNotNull(deserializedModel.mainEntity.mainModel)
            assertNotNull(deserializedModel.mainEntity.armModel)
            assertEquals(2, deserializedModel.mainEntity.textures.size)
            assertEquals(40, deserializedModel.footer.version)
            assertEquals("random1234", deserializedModel.footer.rand)
            assertEquals("OpenYSM", deserializedModel.footer.extra)
        }
    }

    @Test
    fun testCacheDataFailsWithSingleArgConstructor() {
        val defaultPath = Paths.get("src/main/resources/assets/yes_steve_model/builtin/default")
        val rawModel = YSMFolderDeserializer(defaultPath).use { it.deserialize() }
        val serializedBuf = YSMBinarySerializer.serialize(rawModel, 40, true)
        val data = serializedBuf.toArray()

        val result = runCatching {
            YSMBinaryDeserializer(data).use { deserializer ->
                deserializer.deserializeKeepOpen()
            }
        }
        // Since cache data does not include a DWORD format prefix, single-arg constructor misinterprets
        // payload as format number and offset is corrupted, causing failure.
        assert(result.isFailure)
    }
}
