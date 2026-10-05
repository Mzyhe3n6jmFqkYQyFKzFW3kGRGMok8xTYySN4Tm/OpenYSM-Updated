@file:Suppress("unused")

package com.elfmcys.yesstevemodel.resource

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.resource.pojo.RawYsmModel
import com.elfmcys.yesstevemodel.util.DigestUtil
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import rip.ysm.imagestream.avif.AvifDecoder
import rip.ysm.imagestream.webp.WebpDecoder
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.floor
import kotlin.math.min

class YSMFolderDeserializer : AutoCloseable {
    private val readFilesMd5Map: MutableMap<String, String> = TreeMap()
    private var finalFolderHash: String? = null
    private val rootPath: Path?
    private val zipFileSystem: FileSystem?
    private val model: RawYsmModel
    private val inMemoryFiles: Map<String, ByteArray>?

    constructor(sourcePath: Path) {
        if (!Files.exists(sourcePath)) throw FileNotFoundException("Model source not found: $sourcePath")

        inMemoryFiles = null

        if (Files.isDirectory(sourcePath)) {
            rootPath = sourcePath
            zipFileSystem = null
        } else if (sourcePath.toString().endsWith(".zip") || sourcePath.toString().endsWith(".ysm")) {
            val uri = URI.create("jar:" + sourcePath.toUri())
            val fs = FileSystems.newFileSystem(uri, emptyMap<String, Any>())
            zipFileSystem = fs
            rootPath = fs.getPath("/")
        } else {
            throw IllegalArgumentException("Unsupported file type. Expected directory or .zip")
        }

        model = RawYsmModel()
        model.formatVersion = 65535
    }

    constructor(memoryFiles: Map<String, ByteArray>?) {
        inMemoryFiles = memoryFiles
        rootPath = null
        zipFileSystem = null
        model = RawYsmModel()
        model.formatVersion = 65535
    }

    private fun readResource(relativePath: String?): ByteArray? {
        if (relativePath.isNullOrEmpty()) return null
        return runCatching {
            var path = relativePath
            if (path.startsWith("/")) {
                path = path.substring(1)
            }
            val normalizedPath = path.replace('\\', '/')
            var data: ByteArray? = null

            if (inMemoryFiles == null) {
                val target = rootPath?.resolve(path)
                if (target != null && Files.exists(target) && Files.isRegularFile(target)) {
                    data = Files.readAllBytes(target)
                }
            } else {
                data = inMemoryFiles[normalizedPath]
            }

            if (data != null && !readFilesMd5Map.containsKey(normalizedPath)) {
                readFilesMd5Map[normalizedPath] = DigestUtil.md5Hex(data)
            }
            data
        }.getOrElse {
            Constants.LOGGER.warn("Failed to read resource: $relativePath", it)
            null
        }
    }

    fun deserialize(): RawYsmModel {
        val ysmJsonBytes = readResource("ysm.json")
        if (ysmJsonBytes != null) {
            val jsonStr = String(ysmJsonBytes, StandardCharsets.UTF_8)
            val ysmJson = JsonParser.parseString(jsonStr).asJsonObject
            parseYsmJson(ysmJson)
        } else {
            parseLegacyFormat()
        }

        parseGlobalResources()

        finalFolderHash = calculateFinalFolderHash()
        model.properties.sha256 = finalFolderHash ?: ""
        model.footer.version = 65535
        return model
    }

    override fun close() {
        zipFileSystem?.close()
        (inMemoryFiles as? MutableMap<*, *>)?.clear()
    }

    private fun parseYsmJson(ysmJson: JsonObject) {
        if (ysmJson.has("metadata")) parseMetadata(ysmJson.getAsJsonObject("metadata"))
        if (ysmJson.has("properties")) parseProperties(ysmJson.getAsJsonObject("properties"))
        if (ysmJson.has("files")) {
            val files = ysmJson.getAsJsonObject("files")
            if (files.has("player")) parseMainEntity(files.getAsJsonObject("player"))
            if (files.has("vehicles")) parseSubEntities(files.get("vehicles"), model.vehicles, "vehicle")
            if (files.has("projectiles")) parseSubEntities(files.get("projectiles"), model.projectiles, "projectile")
        }
    }

    private fun parseMetadata(metaObj: JsonObject) {
        model.metadata.name = getStr(metaObj, "name", "")
        model.metadata.tips = getStr(metaObj, "tips", "")
        if (metaObj.has("license") && metaObj.get("license").isJsonObject) {
            val licObj = metaObj.getAsJsonObject("license")
            model.metadata.licenseType = getStr(licObj, "type", "")
            model.metadata.licenseDescription = getStr(licObj, "desc", "")
        }

        if (metaObj.has("authors") && metaObj.get("authors").isJsonArray) {
            for (elem in metaObj.getAsJsonArray("authors")) {
                if (!elem.isJsonObject) continue
                val authorObj = elem.asJsonObject
                val author = RawYsmModel.RawMetadata.Author()
                author.name = getStr(authorObj, "name", "")
                author.role = getStr(authorObj, "role", "")
                author.comment = getStr(authorObj, "comment", "")

                if (authorObj.has("contact") && authorObj.get("contact").isJsonObject) {
                    for ((key, value) in authorObj.getAsJsonObject("contact").entrySet()) {
                        author.contacts[key] = value.asString
                    }
                }

                if (authorObj.has("avatar")) {
                    val avatarPath = getStr(authorObj, "avatar", "")
                    if (avatarPath.isNotEmpty()) {
                        val avatarData = readResource(avatarPath)
                        if (avatarData != null) {
                            val meta = parseImageMeta(avatarData, avatarPath)
                            val img = RawYsmModel.RawImage()
                            img.width = meta.width
                            img.height = meta.height
                            img.format = meta.format
                            img.name = author.name
                            img.data = avatarData
                            img.unknownFlag = 1

                            author.avatar = avatarPath
                            author.avatarImage = img
                        }
                    }
                }
                model.metadata.authors.add(author)
            }
        }

        if (metaObj.has("link") && metaObj.get("link").isJsonObject) {
            for ((key, value) in metaObj.getAsJsonObject("link").entrySet()) {
                model.metadata.links[key] = value.asString
            }
        }
    }

    private fun parseProperties(propsObj: JsonObject) {
        model.properties.widthScale = getDouble(propsObj, "width_scale", 0.7).toFloat()
        model.properties.heightScale = getDouble(propsObj, "height_scale", 0.7).toFloat()
        model.properties.defaultTexture = getStr(propsObj, "default_texture", "default")
        model.properties.previewAnimation = getStr(propsObj, "preview_animation", "")
        model.properties.isFree = getBool(propsObj, "free", false)
        model.properties.renderLayersFirst = getBool(propsObj, "render_layers_first", false)
        model.properties.allCutout = getBool(propsObj, "all_cutout", false)
        model.properties.disablePreviewRotation = getBool(propsObj, "disable_preview_rotation", false)
        model.properties.guiNoLighting = getBool(propsObj, "gui_no_lighting", false)
        model.properties.mergeMultilineExpr = getBool(propsObj, "merge_multiline_expr", false)
        model.properties.guiForeground = getStr(propsObj, "gui_foreground", "")
        model.properties.guiBackground = getStr(propsObj, "gui_background", "")
        if (propsObj.has("extra_animation") && propsObj.get("extra_animation").isJsonObject) {
            for ((key, value) in propsObj.getAsJsonObject("extra_animation").entrySet()) {
                model.properties.extraAnimations[key] = value.asString
            }
        }

        if (propsObj.has("extra_animation_classify") && propsObj.get("extra_animation_classify").isJsonArray) {
            for (elem in propsObj.getAsJsonArray("extra_animation_classify")) {
                if (!elem.isJsonObject) continue
                val clsObj = elem.asJsonObject
                val classify = RawYsmModel.ExtraAnimationClassify()
                classify.id = getStr(clsObj, "id", "")
                if (clsObj.has("extra_animation") && clsObj.get("extra_animation").isJsonObject) {
                    for ((key, value) in clsObj.getAsJsonObject("extra_animation").entrySet()) {
                        classify.extras[key] = value.asString
                    }
                }
                model.properties.extraAnimationClassifies.add(classify)
            }
        }

        if (propsObj.has("extra_animation_buttons") && propsObj.get("extra_animation_buttons").isJsonArray) {
            for (elem in propsObj.getAsJsonArray("extra_animation_buttons")) {
                if (!elem.isJsonObject) continue
                val btnObj = elem.asJsonObject
                val btn = RawYsmModel.ExtraAnimationButton()
                btn.id = getStr(btnObj, "id", "")
                btn.name = getStr(btnObj, "name", "")
                btn.description = getStr(btnObj, "description", "")

                if (btnObj.has("config_forms") && btnObj.get("config_forms").isJsonArray) {
                    for (formElem in btnObj.getAsJsonArray("config_forms")) {
                        if (!formElem.isJsonObject) continue
                        val formObj = formElem.asJsonObject
                        val form = RawYsmModel.ConfigForm()
                        form.type = getStr(formObj, "type", "")
                        form.title = getStr(formObj, "title", "")
                        form.description = getStr(formObj, "description", "")
                        form.defaultValue = getStr(formObj, "value", "")
                        form.step = getDouble(formObj, "step", 0.0).toFloat()
                        form.min = getDouble(formObj, "min", 0.0).toFloat()
                        form.max = getDouble(formObj, "max", 0.0).toFloat()
                        if (formObj.has("labels") && formObj.get("labels").isJsonObject) {
                            for ((key, value) in formObj.getAsJsonObject("labels").entrySet()) {
                                form.labels[key] = value.asString
                            }
                        }
                        btn.forms.add(form)
                    }
                }
                model.properties.extraAnimationButtons.add(btn)
            }
        }

        loadGuiImage(model.properties.guiBackground, "gui_background")
        loadGuiImage(model.properties.guiForeground, "gui_foreground")
    }

    private fun loadGuiImage(path: String?, id: String) {
        if (path.isNullOrEmpty()) return
        var data = readResource(path)
        if (data == null) data = readResource("background/$id.png")

        if (data != null) {
            val meta = parseImageMeta(data, path)
            val img = RawYsmModel.RawImage()
            img.width = meta.width
            img.height = meta.height
            img.format = meta.format
            img.name = id
            img.data = data
            img.unknownFlag = 1
            model.properties.backgroundImages.add(img)
        }
    }

    private fun parseMainEntity(playerObj: JsonObject) {
        if (playerObj.has("model") && playerObj.get("model").isJsonObject) {
            val modelObj = playerObj.getAsJsonObject("model")
            if (modelObj.has("main")) {
                val geoData = readResource(modelObj.get("main").asString)
                if (geoData != null) model.mainEntity.mainModel = parseGeometry(geoData, 1)
            }
            if (modelObj.has("arm")) {
                val geoData = readResource(modelObj.get("arm").asString)
                if (geoData != null) model.mainEntity.armModel = parseGeometry(geoData, 2)
            }
        }

        if (playerObj.has("texture")) {
            val texElem = playerObj.get("texture")
            val texArr: Iterable<JsonElement> =
                if (texElem.isJsonArray) texElem.asJsonArray else Collections.singletonList(texElem)
            for (elem in texArr) {
                var texPath: String? = null
                if (elem.isJsonPrimitive) {
                    texPath = elem.asString
                } else if (elem.isJsonObject && elem.asJsonObject.has("uv")) {
                    texPath = elem.asJsonObject.get("uv").asString
                }
                if (texPath == null) continue

                val texData = readResource(texPath)
                if (texData != null) {
                    val meta = parseImageMeta(texData, texPath)
                    val rt = RawYsmModel.RawTexture()
                    rt.hash = DigestUtil.sha256Hex(texData)
                    rt.width = meta.width
                    rt.height = meta.height
                    rt.imageFormat = meta.format
                    rt.name = extractFileName(texPath)
                    rt.data = texData
                    rt.unknownFlag = 1

                    if (elem.isJsonObject) {
                        val obj = elem.asJsonObject
                        if (obj.has("specular")) {
                            val spData = readResource(obj.get("specular").asString)
                            if (spData != null) {
                                val spMeta = parseImageMeta(spData, "specular")
                                val sub = RawYsmModel.RawTexture.SubTexture()
                                sub.specularType = 2
                                sub.data = spData
                                sub.unknownFlag = 1
                                sub.hash = DigestUtil.sha256Hex(spData)
                                sub.width = spMeta.width
                                sub.height = spMeta.height
                                sub.imageFormat = spMeta.format
                                rt.subTextures.add(sub)
                            }
                        }
                        if (obj.has("normal")) {
                            val nrData = readResource(obj.get("normal").asString)
                            if (nrData != null) {
                                val nrMeta = parseImageMeta(nrData, "normal")
                                val sub = RawYsmModel.RawTexture.SubTexture()
                                sub.specularType = 1
                                sub.data = nrData
                                sub.unknownFlag = 1
                                sub.hash = DigestUtil.sha256Hex(nrData)
                                sub.width = nrMeta.width
                                sub.height = nrMeta.height
                                sub.imageFormat = nrMeta.format
                                rt.subTextures.add(sub)
                            }
                        }
                    }
                    model.mainEntity.textures[rt.name ?: ""] = rt
                }
            }
        }

        if (playerObj.has("animation") && playerObj.get("animation").isJsonObject) {
            val animObj = playerObj.getAsJsonObject("animation")
            for ((key, value) in animObj.entrySet()) {
                val animData = readResource(value.asString)
                if (animData != null) {
                    val raf = parseAnimations(animData)
                    raf.fileHash = DigestUtil.sha256Hex(animData)
                    raf.animType = getAnimTypeFromKey(key)
                    model.mainEntity.animationFiles[key] = raf
                }
            }
        }
        if (playerObj.has("animation_controllers") && playerObj.get("animation_controllers").isJsonArray) {
            for (acElem in playerObj.getAsJsonArray("animation_controllers")) {
                val acPath = acElem.asString
                val acData = readResource(acPath)
                if (acData != null) {
                    val acHash = DigestUtil.sha256Hex(acData)
                    val acFile = RawYsmModel.RawAnimationControllerFile()
                    acFile.name = extractFileName(acPath)
                    acFile.hash = acHash
                    parseAnimationControllers(acData, acFile.controllers)
                    model.mainEntity.animationControllerFiles.add(acFile)
                }
            }
        }
    }

    private fun parseSubEntities(
        sectionElem: JsonElement,
        targetMap: MutableMap<String, RawYsmModel.RawSubEntity>,
        defaultIdentifier: String
    ) {
        if (!sectionElem.isJsonArray && !sectionElem.isJsonObject) return
        val items = ArrayList<JsonObject>()

        if (sectionElem.isJsonArray) {
            for (e in sectionElem.asJsonArray) {
                if (e.isJsonObject) items.add(e.asJsonObject)
            }
        } else {
            val mapObj = sectionElem.asJsonObject
            for ((key, value) in mapObj.entrySet()) {
                if (value.isJsonObject) {
                    val item = value.asJsonObject
                    if (!item.has("match")) item.addProperty("__temp_identifier", key)
                    items.add(item)
                }
            }
        }

        for ((index, item) in items.withIndex()) {
            val sub = RawYsmModel.RawSubEntity()
            sub.identifier =
                if (item.has("__temp_identifier")) item.get("__temp_identifier").asString else "${defaultIdentifier}_$index"

            if (item.has("match")) {
                val match = item.get("match")
                if (match.isJsonArray) {
                    val mArr = match.asJsonArray
                    sub.matchIds = Array(mArr.size()) { mArr.get(it).asString }
                } else if (match.isJsonPrimitive) {
                    sub.matchIds = arrayOf(match.asString)
                }
            }

            if (item.has("model")) {
                val geoData = readResource(item.get("model").asString)
                if (geoData != null) sub.model = parseGeometry(geoData, 3)
            }

            if (item.has("texture")) {
                val texPath = if (item.get("texture").isJsonObject) item.getAsJsonObject("texture")
                    .get("uv").asString else item.get("texture").asString
                val texData = readResource(texPath)
                if (texData != null) {
                    val meta = parseImageMeta(texData, texPath)
                    val rt = RawYsmModel.RawTexture()

                    rt.hash = DigestUtil.sha256Hex(texData)
                    rt.width = meta.width
                    rt.height = meta.height
                    rt.imageFormat = meta.format

                    rt.name = "base_texture_$index"
                    rt.data = texData
                    rt.unknownFlag = 1
                    sub.textures[rt.name ?: ""] = rt
                }
            }

            if (item.has("animation")) {
                val animData = readResource(item.get("animation").asString)
                if (animData != null) {
                    val raf = parseAnimations(animData)
                    raf.fileHash = DigestUtil.sha256Hex(animData)
                    raf.animType = getAnimTypeFromKey("extra")
                    sub.animationFiles["sub_anim"] = raf
                }
            }

            if (item.has("controller")) {
                val acPath = item.get("controller").asString
                val acData = readResource(acPath)
                if (acData != null) {
                    val acHash = DigestUtil.sha256Hex(acData)
                    val acFile = RawYsmModel.RawAnimationControllerFile()
                    acFile.name = extractFileName(acPath)
                    acFile.hash = acHash
                    parseAnimationControllers(acData, acFile.controllers)
                    sub.animationControllerFiles.add(acFile)
                }
            }

            targetMap[sub.identifier ?: ""] = sub
        }
    }

    private fun parseGeometry(data: ByteArray, modelType: Int): RawYsmModel.RawGeometry {
        val json = String(data, StandardCharsets.UTF_8)
        val root = JsonParser.parseString(json).asJsonObject
        val geometries = if (root.has("minecraft:geometry")) root.getAsJsonArray("minecraft:geometry") else null
        if (geometries == null || geometries.isEmpty) return RawYsmModel.RawGeometry()

        val geoObj = geometries.get(0).asJsonObject
        val geo = RawYsmModel.RawGeometry()
        geo.sha256 = DigestUtil.sha256Hex(data)

        geo.modelType = modelType
        geo.unkFloat1 = 0.7f
        geo.unkFloat2 = 0.7f

        if (geoObj.has("description")) {
            val desc = geoObj.getAsJsonObject("description")
            geo.identifier = getStr(desc, "identifier", "")
            geo.textureWidth = getDouble(desc, "texture_width", 64.0).toFloat()
            geo.textureHeight = getDouble(desc, "texture_height", 64.0).toFloat()
            geo.visibleBoundsWidth = getDouble(desc, "visible_bounds_width", 0.0).toFloat()
            geo.visibleBoundsHeight = getDouble(desc, "visible_bounds_height", 0.0).toFloat()
            if (desc.has("visible_bounds_offset") && desc.get("visible_bounds_offset").isJsonArray) {
                val offsetArr = desc.getAsJsonArray("visible_bounds_offset")
                geo.visibleBoundsOffset = FloatArray(offsetArr.size()) { offsetArr.get(it).asFloat }
            } else {
                geo.visibleBoundsOffset = FloatArray(0)
            }

            if (modelType == 1 && desc.has("ysm_extra_info")) {
                parseLegacyMetadata(desc.getAsJsonObject("ysm_extra_info"), false)
            }
        }

        if (geoObj.has("bones") && geoObj.get("bones").isJsonArray) {
            for (boneElem in geoObj.getAsJsonArray("bones")) {
                if (!boneElem.isJsonObject) continue
                val bObj = boneElem.asJsonObject
                val bone = RawYsmModel.RawBone()
                bone.name = getStr(bObj, "name", "")
                bone.parentName = getStr(bObj, "parent", "")

                if (bObj.has("pivot")) {
                    val pivot = bObj.getAsJsonArray("pivot")
                    bone.pivot = floatArrayOf(-pivot.get(0).asFloat, pivot.get(1).asFloat, pivot.get(2).asFloat)
                }
                if (bObj.has("rotation")) {
                    val rot = bObj.getAsJsonArray("rotation")
                    bone.rotation = floatArrayOf(
                        -Math.toRadians(rot.get(0).asDouble).toFloat(),
                        -Math.toRadians(rot.get(1).asDouble).toFloat(),
                        Math.toRadians(rot.get(2).asDouble).toFloat()
                    )
                }

                val boneInflate = getDouble(bObj, "inflate", 0.0).toFloat()
                val boneMirror = getBool(bObj, "mirror", false)

                if (bObj.has("cubes") && bObj.get("cubes").isJsonArray) {
                    for (cElem in bObj.getAsJsonArray("cubes")) {
                        if (!cElem.isJsonObject) continue
                        val cObj = cElem.asJsonObject
                        val cube = RawYsmModel.RawCube()

                        val inflate = if (cObj.has("inflate")) cObj.get("inflate").asFloat else boneInflate
                        val mirror = if (cObj.has("mirror")) cObj.get("mirror").asBoolean else boneMirror

                        val origin = getFloatArray(cObj, "origin", 3)
                        val size = getFloatArray(cObj, "size", 3)

                        val cx = -origin[0] - size[0] - inflate
                        val cy = origin[1] - inflate
                        val cz = origin[2] - inflate
                        val cw = size[0] + inflate * 2
                        val ch = size[1] + inflate * 2
                        val cd = size[2] + inflate * 2

                        val cubeBakeMat = Matrix4f()
                        if (cObj.has("rotation") || cObj.has("pivot")) {
                            val cpvt = getFloatArray(cObj, "pivot", 3)
                            val crot = getFloatArray(cObj, "rotation", 3)
                            cubeBakeMat.translate(-cpvt[0] / 16f, cpvt[1] / 16f, cpvt[2] / 16f)
                            cubeBakeMat.rotateZ(Math.toRadians(crot[2].toDouble()).toFloat())
                            cubeBakeMat.rotateY(-Math.toRadians(crot[1].toDouble()).toFloat())
                            cubeBakeMat.rotateX(-Math.toRadians(crot[0].toDouble()).toFloat())
                            cubeBakeMat.translate(cpvt[0] / 16f, -cpvt[1] / 16f, -cpvt[2] / 16f)
                        }
                        val cubeNormalMat = Matrix3f()
                        cubeBakeMat.normal(cubeNormalMat)

                        if (cObj.has("uv")) {
                            val uvElem = cObj.get("uv")
                            if (uvElem.isJsonObject) {
                                val uvObj = uvElem.asJsonObject
                                bakeFaceToRaw(
                                    cube,
                                    uvObj,
                                    "north",
                                    "north",
                                    mirror,
                                    cx,
                                    cy,
                                    cz,
                                    cw,
                                    ch,
                                    cd,
                                    geo.textureWidth,
                                    geo.textureHeight,
                                    Vector3f(0f, 0f, -1f),
                                    cubeBakeMat,
                                    cubeNormalMat
                                )
                                bakeFaceToRaw(
                                    cube,
                                    uvObj,
                                    "south",
                                    "south",
                                    mirror,
                                    cx,
                                    cy,
                                    cz,
                                    cw,
                                    ch,
                                    cd,
                                    geo.textureWidth,
                                    geo.textureHeight,
                                    Vector3f(0f, 0f, 1f),
                                    cubeBakeMat,
                                    cubeNormalMat
                                )
                                bakeFaceToRaw(
                                    cube,
                                    uvObj,
                                    "east",
                                    if (mirror) "west" else "east",
                                    mirror,
                                    cx,
                                    cy,
                                    cz,
                                    cw,
                                    ch,
                                    cd,
                                    geo.textureWidth,
                                    geo.textureHeight,
                                    Vector3f(1f, 0f, 0f),
                                    cubeBakeMat,
                                    cubeNormalMat
                                )
                                bakeFaceToRaw(
                                    cube,
                                    uvObj,
                                    "west",
                                    if (mirror) "east" else "west",
                                    mirror,
                                    cx,
                                    cy,
                                    cz,
                                    cw,
                                    ch,
                                    cd,
                                    geo.textureWidth,
                                    geo.textureHeight,
                                    Vector3f(-1f, 0f, 0f),
                                    cubeBakeMat,
                                    cubeNormalMat
                                )
                                bakeFaceToRaw(
                                    cube,
                                    uvObj,
                                    "up",
                                    "up",
                                    mirror,
                                    cx,
                                    cy,
                                    cz,
                                    cw,
                                    ch,
                                    cd,
                                    geo.textureWidth,
                                    geo.textureHeight,
                                    Vector3f(0f, 1f, 0f),
                                    cubeBakeMat,
                                    cubeNormalMat
                                )
                                bakeFaceToRaw(
                                    cube,
                                    uvObj,
                                    "down",
                                    "down",
                                    mirror,
                                    cx,
                                    cy,
                                    cz,
                                    cw,
                                    ch,
                                    cd,
                                    geo.textureWidth,
                                    geo.textureHeight,
                                    Vector3f(0f, -1f, 0f),
                                    cubeBakeMat,
                                    cubeNormalMat
                                )
                            } else if (uvElem.isJsonArray) {
                                val uvArr = uvElem.asJsonArray
                                val uvX = uvArr.get(0).asFloat
                                val uvY = uvArr.get(1).asFloat
                                val dx = floor(size[0].toDouble()).toFloat()
                                val dy = floor(size[1].toDouble()).toFloat()
                                val dz = floor(size[2].toDouble()).toFloat()

                                val fakeUvObj = JsonObject()
                                fakeUvObj.add("north", createFaceUVNode(uvX + dz, uvY + dz, dx, dy))
                                fakeUvObj.add("south", createFaceUVNode(uvX + dz + dx + dz, uvY + dz, dx, dy))
                                fakeUvObj.add("east", createFaceUVNode(uvX, uvY + dz, dz, dy))
                                fakeUvObj.add("west", createFaceUVNode(uvX + dz + dx, uvY + dz, dz, dy))
                                fakeUvObj.add("up", createFaceUVNode(uvX + dz, uvY, dx, dz))
                                fakeUvObj.add("down", createFaceUVNode(uvX + dz + dx, uvY + dz, dx, -dz))

                                bakeFaceToRaw(
                                    cube,
                                    fakeUvObj,
                                    "north",
                                    "north",
                                    mirror,
                                    cx,
                                    cy,
                                    cz,
                                    cw,
                                    ch,
                                    cd,
                                    geo.textureWidth,
                                    geo.textureHeight,
                                    Vector3f(0f, 0f, -1f),
                                    cubeBakeMat,
                                    cubeNormalMat
                                )
                                bakeFaceToRaw(
                                    cube,
                                    fakeUvObj,
                                    "south",
                                    "south",
                                    mirror,
                                    cx,
                                    cy,
                                    cz,
                                    cw,
                                    ch,
                                    cd,
                                    geo.textureWidth,
                                    geo.textureHeight,
                                    Vector3f(0f, 0f, 1f),
                                    cubeBakeMat,
                                    cubeNormalMat
                                )
                                bakeFaceToRaw(
                                    cube,
                                    fakeUvObj,
                                    "east",
                                    if (mirror) "west" else "east",
                                    mirror,
                                    cx,
                                    cy,
                                    cz,
                                    cw,
                                    ch,
                                    cd,
                                    geo.textureWidth,
                                    geo.textureHeight,
                                    Vector3f(1f, 0f, 0f),
                                    cubeBakeMat,
                                    cubeNormalMat
                                )
                                bakeFaceToRaw(
                                    cube,
                                    fakeUvObj,
                                    "west",
                                    if (mirror) "east" else "west",
                                    mirror,
                                    cx,
                                    cy,
                                    cz,
                                    cw,
                                    ch,
                                    cd,
                                    geo.textureWidth,
                                    geo.textureHeight,
                                    Vector3f(-1f, 0f, 0f),
                                    cubeBakeMat,
                                    cubeNormalMat
                                )
                                bakeFaceToRaw(
                                    cube,
                                    fakeUvObj,
                                    "up",
                                    "up",
                                    mirror,
                                    cx,
                                    cy,
                                    cz,
                                    cw,
                                    ch,
                                    cd,
                                    geo.textureWidth,
                                    geo.textureHeight,
                                    Vector3f(0f, 1f, 0f),
                                    cubeBakeMat,
                                    cubeNormalMat
                                )
                                bakeFaceToRaw(
                                    cube,
                                    fakeUvObj,
                                    "down",
                                    "down",
                                    mirror,
                                    cx,
                                    cy,
                                    cz,
                                    cw,
                                    ch,
                                    cd,
                                    geo.textureWidth,
                                    geo.textureHeight,
                                    Vector3f(0f, -1f, 0f),
                                    cubeBakeMat,
                                    cubeNormalMat
                                )
                            }
                        }
                        bone.cubes.add(cube)
                    }
                }
                geo.bones.add(bone)
            }
        }
        return geo
    }

    private fun bakeFaceToRaw(
        cube: RawYsmModel.RawCube,
        uvObj: JsonObject,
        faceType: String,
        uvFaceName: String,
        mirror: Boolean,
        x: Float,
        y: Float,
        z: Float,
        w: Float,
        h: Float,
        d: Float,
        tw: Float,
        th: Float,
        rawNormal: Vector3f,
        cubeBakeMat: Matrix4f,
        cubeNormalMat: Matrix3f
    ) {
        if (!uvObj.has(uvFaceName)) return
        val faceData = uvObj.getAsJsonObject(uvFaceName)
        val uv = getFloatArray(faceData, "uv", 2)
        val uvSize = getFloatArray(faceData, "uv_size", 2)

        var u0 = uv[0] / tw
        val v0 = uv[1] / th
        var u1 = (uv[0] + uvSize[0]) / tw
        val v1 = (uv[1] + uvSize[1]) / th

        if (!mirror) {
            val temp = u0
            u0 = u1
            u1 = temp
        }

        val face = RawYsmModel.RawFace()
        val bakedNormal = Vector3f(rawNormal).mul(cubeNormalMat).normalize()
        face.normal = floatArrayOf(bakedNormal.x, bakedNormal.y, bakedNormal.z)

        val x1 = x / 16f
        val x2 = (x + w) / 16f
        val y1 = y / 16f
        val y2 = (y + h) / 16f
        val z1 = z / 16f
        val z2 = (z + d) / 16f

        val p1 = Vector3f(x1, y1, z1)
        val p2 = Vector3f(x1, y1, z2)
        val p3 = Vector3f(x1, y2, z1)
        val p4 = Vector3f(x1, y2, z2)
        val p5 = Vector3f(x2, y1, z1)
        val p6 = Vector3f(x2, y1, z2)
        val p7 = Vector3f(x2, y2, z1)
        val p8 = Vector3f(x2, y2, z2)

        val positions = when (faceType) {
            "west" -> arrayOf(p4, p3, p1, p2)
            "east" -> arrayOf(p7, p8, p6, p5)
            "north" -> arrayOf(p3, p7, p5, p1)
            "south" -> arrayOf(p8, p4, p2, p6)
            "up" -> arrayOf(p4, p8, p7, p3)
            "down" -> arrayOf(p1, p5, p6, p2)
            else -> return
        }

        val tempPos = Vector4f()
        for (i in 0 until 4) {
            tempPos.set(positions[i].x(), positions[i].y(), positions[i].z(), 1.0f).mul(cubeBakeMat)
            face.positions[i] = floatArrayOf(tempPos.x(), tempPos.y(), tempPos.z())
        }

        face.u = floatArrayOf(u0, u1, u1, u0)
        face.v = floatArrayOf(v0, v0, v1, v1)
        cube.faces.add(face)
    }

    private fun createFaceUVNode(u: Float, v: Float, w: Float, h: Float): JsonObject {
        val node = JsonObject()
        val uv = JsonArray()
        uv.add(u)
        uv.add(v)
        val size = JsonArray()
        size.add(w)
        size.add(h)
        node.add("uv", uv)
        node.add("uv_size", size)
        return node
    }

    private fun parseAnimations(data: ByteArray): RawYsmModel.RawAnimationFile {
        val json = String(data, StandardCharsets.UTF_8)
        val root = JsonParser.parseString(json).asJsonObject
        val raf = RawYsmModel.RawAnimationFile()

        if (root.has("animations")) {
            val anims = root.getAsJsonObject("animations")
            for ((key, value) in anims.entrySet()) {
                if (!value.isJsonObject) continue
                val aObj = value.asJsonObject
                val anim = RawYsmModel.RawAnimation()
                anim.name = key
                anim.length = getDouble(aObj, "animation_length", Float.POSITIVE_INFINITY.toDouble()).toFloat()

                if (aObj.has("loop")) {
                    val loopStr = aObj.get("loop").asString
                    anim.loopMode = when (loopStr) {
                        "true" -> 1
                        "hold_on_last_frame" -> 3
                        else -> 0
                    }
                } else {
                    anim.loopMode = 2
                }

                if (aObj.has("blend_weight")) {
                    val bw = aObj.get("blend_weight")
                    anim.blendWeight = if (bw.isJsonPrimitive && bw.asJsonPrimitive.isNumber) {
                        bw.asFloat
                    } else {
                        bw.asString
                    }
                }

                if (aObj.has("bones") && aObj.get("bones").isJsonObject) {
                    val bonesObj = aObj.getAsJsonObject("bones")
                    for ((bKey, bValue) in bonesObj.entrySet()) {
                        if (!bValue.isJsonObject) continue
                        val bObj = bValue.asJsonObject
                        val ba = RawYsmModel.RawBoneAnimation()
                        ba.boneName = bKey

                        parseChannelToKeyframes(bObj, "rotation", ba.rotation)
                        parseChannelToKeyframes(bObj, "position", ba.position)
                        parseChannelToKeyframes(bObj, "scale", ba.scale)

                        anim.boneAnimations.add(ba)
                    }
                }

                if (aObj.has("timeline") && aObj.get("timeline").isJsonObject) {
                    val tlObj = aObj.getAsJsonObject("timeline")
                    for ((tlKey, tlVal) in tlObj.entrySet()) {
                        val tle = RawYsmModel.RawTimelineEvent()
                        tle.timestamp = tlKey.toFloat()
                        val arr: Iterable<JsonElement> =
                            if (tlVal.isJsonArray) tlVal.asJsonArray else Collections.singletonList(tlVal)
                        for (e in arr) tle.events.add(e.asString)
                        anim.timelineEvents.add(tle)
                    }
                }

                if (aObj.has("sound_effects") && aObj.get("sound_effects").isJsonObject) {
                    val sfxObj = aObj.getAsJsonObject("sound_effects")
                    for ((sfxKey, sfxVal) in sfxObj.entrySet()) {
                        val sfx = RawYsmModel.RawSoundEffect()
                        sfx.timestamp = sfxKey.toFloat()
                        sfx.effectName = getStr(sfxVal.asJsonObject, "effect", "")
                        anim.soundEffects.add(sfx)
                    }
                }

                raf.animations[anim.name ?: ""] = anim
            }
        }
        return raf
    }

    private fun parseChannelToKeyframes(
        bObj: JsonObject,
        channel: String,
        targetList: MutableList<RawYsmModel.RawKeyframe>
    ) {
        if (!bObj.has(channel)) return
        val cElem = bObj.get(channel)

        if (!cElem.isJsonObject) {
            val kf = RawYsmModel.RawKeyframe()
            kf.timestamp = 0.0f
            kf.interpolationMode = 0 // linear
            kf.hasPreData = false
            kf.postData = jsonElementToMolangArray(cElem)
            targetList.add(kf)
            return
        }

        val kfsObj = cElem.asJsonObject
        val sorted = ArrayList(kfsObj.entrySet())
        sorted.sortWith(Comparator.comparingDouble { it.key.toDouble() })

        for ((key, valElem) in sorted) {
            val kf = RawYsmModel.RawKeyframe()
            kf.timestamp = key.toFloat()
            kf.interpolationMode = 0

            if (valElem.isJsonObject) {
                val obj = valElem.asJsonObject
                if (obj.has("lerp_mode")) {
                    val lm = obj.get("lerp_mode").asString
                    kf.interpolationMode = when (lm) {
                        "catmullrom" -> 2
                        "step" -> 1
                        else -> 0
                    }
                } else {
                    kf.interpolationMode = 1
                }

                if (obj.has("pre") && obj.has("post")) {
                    kf.hasPreData = true
                    kf.preData = jsonElementToMolangArray(obj.get("pre"))
                    kf.postData = jsonElementToMolangArray(obj.get("post"))
                } else {
                    kf.hasPreData = false
                    kf.postData =
                        jsonElementToMolangArray(if (obj.has("post")) obj.get("post") else if (obj.has("pre")) obj.get("pre") else obj)
                }
            } else {
                kf.hasPreData = false
                kf.postData = jsonElementToMolangArray(valElem)
            }
            targetList.add(kf)
        }
    }

    private fun jsonElementToMolangArray(elem: JsonElement?): Array<Any?> {
        val arr = arrayOf<Any?>(0f, 0f, 0f)
        if (elem == null || elem.isJsonNull) return arr

        if (elem.isJsonArray) {
            val jArr = elem.asJsonArray
            for (i in 0 until min(3, jArr.size())) {
                val e = jArr.get(i)
                arr[i] = if (e.isJsonPrimitive && e.asJsonPrimitive.isNumber) e.asFloat else e.asString
            }
        } else {
            val v: Any = if (elem.isJsonPrimitive && elem.asJsonPrimitive.isNumber) elem.asFloat else elem.asString
            arr[0] = v
            arr[1] = v
            arr[2] = v
        }
        return arr
    }

    private fun parseAnimationControllers(
        data: ByteArray,
        targetMap: MutableMap<String, RawYsmModel.RawAnimationController>
    ) {
        val json = String(data, StandardCharsets.UTF_8)
        val root = JsonParser.parseString(json).asJsonObject

        if (!root.has("animation_controllers")) return
        val acs = root.getAsJsonObject("animation_controllers")

        for ((key, value) in acs.entrySet()) {
            if (!value.isJsonObject) continue
            val acObj = value.asJsonObject

            val ac = RawYsmModel.RawAnimationController()
            ac.animationName = key
            ac.initialState = getStr(acObj, "initial_state", "default")

            if (acObj.has("states") && acObj.get("states").isJsonObject) {
                val statesObj = acObj.getAsJsonObject("states")
                for ((sKey, sValue) in statesObj.entrySet()) {
                    if (!sValue.isJsonObject) continue
                    val sObj = sValue.asJsonObject

                    val state = RawYsmModel.RawControllerState()
                    state.name = sKey

                    if (sObj.has("animations") && sObj.get("animations").isJsonArray) {
                        for (ae in sObj.getAsJsonArray("animations")) {
                            if (ae.isJsonPrimitive) {
                                state.animations[ae.asString] = ""
                            } else if (ae.isJsonObject) {
                                for ((objKey, objVal) in ae.asJsonObject.entrySet()) {
                                    state.animations[objKey] = objVal.asString
                                }
                            }
                        }
                    }

                    if (sObj.has("transitions") && sObj.get("transitions").isJsonArray) {
                        for (te in sObj.getAsJsonArray("transitions")) {
                            if (te.isJsonObject) {
                                for ((objKey, objVal) in te.asJsonObject.entrySet()) {
                                    state.transitions[objKey] = objVal.asString
                                }
                            }
                        }
                    }

                    if (sObj.has("on_entry") && sObj.get("on_entry").isJsonArray) {
                        for (oe in sObj.getAsJsonArray("on_entry")) state.onEntry.add(oe.asString)
                    }

                    if (sObj.has("on_exit") && sObj.get("on_exit").isJsonArray) {
                        for (oe in sObj.getAsJsonArray("on_exit")) state.onExit.add(oe.asString)
                    }

                    if (sObj.has("sound_effects") && sObj.get("sound_effects").isJsonArray) {
                        for (se in sObj.getAsJsonArray("sound_effects")) {
                            if (se.isJsonObject) {
                                state.soundEffects.add(getStr(se.asJsonObject, "effect", ""))
                            } else if (se.isJsonPrimitive) {
                                state.soundEffects.add(se.asString)
                            }
                        }
                    }

                    if (sObj.has("blend_transition")) {
                        val btElem = sObj.get("blend_transition")
                        if (btElem.isJsonPrimitive && btElem.asJsonPrimitive.isNumber) {
                            state.blendTransitionValue = btElem.asFloat
                        } else if (btElem.isJsonObject) {
                            for ((btKey, btVal) in btElem.asJsonObject.entrySet()) {
                                state.blendTransitions[btKey.toFloat()] = btVal.asFloat
                            }
                        }
                    }

                    ac.states.add(state)
                }
            }
            targetMap[ac.animationName ?: ""] = ac
        }
    }

    private fun parseGlobalResources() {
        when {
            inMemoryFiles != null -> {
                for ((key, value) in inMemoryFiles) {
                    processGlobalResourceFile(key, value)
                }
            }

            else -> {
                val root = rootPath ?: return
                try {
                    Files.walk(root).use { stream ->
                        stream.filter { Files.isRegularFile(it) }.forEach { path ->
                            val relativePath = root.relativize(path).toString().replace('\\', '/')
                            val data = readResource(relativePath)
                            if (data != null) {
                                processGlobalResourceFile(relativePath, data)
                            }
                        }
                    }
                } catch (e: IOException) {
                    Constants.LOGGER.warn("Failed to scan global resources. " + e.message)
                }
            }
        }
    }

    private fun processGlobalResourceFile(relativePath: String, data: ByteArray) {
        if (relativePath.startsWith("sounds/") || relativePath.endsWith(".ogg")) {
            val soundName = extractFileName(relativePath)
            val hash = DigestUtil.sha256Hex(data)
            model.soundFiles[soundName] = RawYsmModel.RawDataFile(hash, data)
        } else if (relativePath.startsWith("lang/") && relativePath.endsWith(".json")) {
            val rawLocale = relativePath.substring("lang/".length, relativePath.length - 5)
            val normalizedLocale = rawLocale.lowercase(Locale.ROOT).replace('-', '_')
            runCatching {
                val hash = DigestUtil.sha256Hex(data)
                val langJsonStr = String(data, StandardCharsets.UTF_8)
                val langJson = JsonParser.parseString(langJsonStr).asJsonObject
                val langMap = LinkedHashMap<String, String>()
                for ((key, value) in langJson.entrySet()) {
                    if (value.isJsonPrimitive) {
                        langMap[key] = value.asString
                    }
                }
                val rawLangFile = RawYsmModel.RawLanguageFile(hash, langMap)
                model.languageFiles[rawLocale] = rawLangFile
                if (normalizedLocale != rawLocale) {
                    model.languageFiles[normalizedLocale] = rawLangFile
                }
            }
        } else if (relativePath.startsWith("functions/") && relativePath.endsWith(".molang")) {
            val fnName = extractFileName(relativePath)
            val hash = DigestUtil.sha256Hex(data)
            model.functionFiles[fnName] = RawYsmModel.RawDataFile(hash, data)
        }
    }

    private data class ImageMeta(val width: Int, val height: Int, val format: Int)

    private fun parseImageMeta(data: ByteArray?, path: String): ImageMeta {
        if (data == null || data.size < 8) {
            throw RuntimeException("Invalid image data. File too small: $path")
        }

        val format = detectFormat(data)
        if (format == 0) {
            throw RuntimeException("Unsupported image format for: $path")
        }

        if (format == 2 && data.size >= 24) {
            val w = ((data[16].toInt() and 0xFF) shl 24) or
                    ((data[17].toInt() and 0xFF) shl 16) or
                    ((data[18].toInt() and 0xFF) shl 8) or
                    (data[19].toInt() and 0xFF)
            val h = ((data[20].toInt() and 0xFF) shl 24) or
                    ((data[21].toInt() and 0xFF) shl 16) or
                    ((data[22].toInt() and 0xFF) shl 8) or
                    (data[23].toInt() and 0xFF)
            return ImageMeta(w, h, format)
        }

        try {
            var img: BufferedImage? = null
            when (format) {
                1, 3 -> img = ImageIO.read(ByteArrayInputStream(data))
                4 -> img = WebpDecoder().read(data)
                5 -> img = AvifDecoder().read(data)
            }
            if (img != null) return ImageMeta(img.width, img.height, format)
            throw RuntimeException("Failed to decode image dimensions for: $path")
        } catch (e: Exception) {
            throw RuntimeException("Error processing image: $path", e)
        }
    }

    private fun calculateFinalFolderHash(): String = runCatching {
        val digest = MessageDigest.getInstance("MD5")
        for ((key, value) in readFilesMd5Map) {
            digest.update(key.toByteArray(StandardCharsets.UTF_8))
            digest.update(value.toByteArray(StandardCharsets.UTF_8))
        }
        val hash = digest.digest()
        val hexString = StringBuilder(32)
        for (b in hash) {
            val hex = Integer.toHexString(0xff and b.toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
        }
        hexString.toString()
    }.getOrElse { "" }

    fun getFolderHash(): String? = finalFolderHash

    private fun parseLegacyFormat() {
        val mainData = readResource("main.json") ?: throw RuntimeException("Legacy model missing main.json")
        val armData = readResource("arm.json") ?: throw RuntimeException("Legacy model missing arm.json")

        val pngFiles = ArrayList<String>()
        if (inMemoryFiles != null) {
            for (pathKey in inMemoryFiles.keys) {
                if (pathKey.endsWith(".png") && !pathKey.contains("/")) {
                    pngFiles.add(pathKey)
                }
            }
        } else {
            val root = rootPath
            if (root != null) {
                try {
                    Files.list(root).use { stream ->
                        stream.filter { Files.isRegularFile(it) }.forEach { path ->
                            val fileName = path.fileName.toString()
                            if (fileName.endsWith(".png")) {
                                pngFiles.add(fileName)
                            }
                        }
                    }
                } catch (e: IOException) {
                    Constants.LOGGER.error("Failed to read PNG files from YSM folder", e)
                }
            }
        }

        var hasMainTexture = false
        for (texName in pngFiles) {
            if (texName != "arrow.png") {
                hasMainTexture = true
                break
            }
        }

        if (!hasMainTexture) {
            throw RuntimeException("Legacy model requires at least one texture.")
        }

        val arrowData = readResource("arrow.json")
        if (arrowData != null && !pngFiles.contains("arrow.png")) {
            throw RuntimeException("arrow.json is present but arrow.png is missing.")
        }

        val infoData = readResource("info.json")
        if (infoData != null) {
            runCatching {
                val infoObj = JsonParser.parseString(String(infoData, StandardCharsets.UTF_8)).asJsonObject
                parseLegacyMetadata(infoObj, true)
            }.onFailure {
                Constants.LOGGER.error("Failed to parse info.json", it)
            }
        }

        model.mainEntity.mainModel = parseGeometry(mainData, 1)
        model.mainEntity.armModel = parseGeometry(armData, 2)

        for (texName in pngFiles) {
            if (texName == "arrow.png") continue
            val texData = readResource(texName)
            if (texData != null) {
                val meta = parseImageMeta(texData, texName)
                val rt = RawYsmModel.RawTexture()
                rt.hash = DigestUtil.sha256Hex(texData)
                rt.width = meta.width
                rt.height = meta.height
                rt.imageFormat = meta.format
                rt.name = extractFileName(texName)
                rt.data = texData
                rt.unknownFlag = 1
                model.mainEntity.textures[rt.name ?: ""] = rt
            }
        }

        if (model.mainEntity.textures.isNotEmpty()) {
            model.properties.defaultTexture = model.mainEntity.textures.keys.iterator().next()
        }

        val animFiles = arrayOf(
            "main.animation.json",
            "arm.animation.json",
            "extra.animation.json",
            "tac.animation.json",
            "carryon.animation.json",
            "slashblade.animation.json",
            "tlm.animation.json"
        )
        for (fileName in animFiles) {
            val animData = readResource(fileName)
            if (animData != null) {
                val raf = parseAnimations(animData)
                raf.fileHash = DigestUtil.sha256Hex(animData)

                val animKey = fileName.substring(0, fileName.length - ".animation.json".length)
                raf.animType = getAnimTypeFromKey(animKey)
                model.mainEntity.animationFiles[animKey] = raf
            }
        }

        if (arrowData != null) {
            val arrowSub = RawYsmModel.RawSubEntity()
            arrowSub.identifier = "arrow"
            arrowSub.model = parseGeometry(arrowData, 3)

            val arrowTexData = readResource("arrow.png")
            if (arrowTexData != null) {
                val meta = parseImageMeta(arrowTexData, "arrow.png")
                val rt = RawYsmModel.RawTexture()
                rt.hash = DigestUtil.sha256Hex(arrowTexData)
                rt.width = meta.width
                rt.height = meta.height
                rt.imageFormat = meta.format
                rt.name = "arrow"
                rt.data = arrowTexData
                rt.unknownFlag = 1
                arrowSub.textures[rt.name ?: ""] = rt
            }

            val arrowAnimData = readResource("arrow.animation.json")
            if (arrowAnimData != null) {
                val raf = parseAnimations(arrowAnimData)
                raf.fileHash = DigestUtil.sha256Hex(arrowAnimData)
                raf.animType = getAnimTypeFromKey("arrow")
                arrowSub.animationFiles["sub_anim"] = raf
            }

            model.projectiles["arrow"] = arrowSub
        }
    }

    private fun parseLegacyMetadata(infoObj: JsonObject?, overwrite: Boolean) {
        if (infoObj == null) return
        if (infoObj.has("name") && (overwrite || model.metadata.name.isEmpty())) {
            model.metadata.name = getStr(infoObj, "name", "")
        }
        if (infoObj.has("tips") && (overwrite || model.metadata.tips.isEmpty())) {
            model.metadata.tips = getStr(infoObj, "tips", "")
        }
        if (infoObj.has("license") && (overwrite || model.metadata.licenseDescription.isEmpty())) {
            model.metadata.licenseDescription = getStr(infoObj, "license", "")
        }
        if (infoObj.has("free")) {
            if (overwrite || !model.properties.isFree) {
                model.properties.isFree = getBool(infoObj, "free", false)
            }
        }

        if (infoObj.has("authors") && infoObj.get("authors").isJsonArray) {
            if (overwrite || model.metadata.authors.isEmpty()) {
                model.metadata.authors.clear()
                for (e in infoObj.getAsJsonArray("authors")) {
                    val author = RawYsmModel.RawMetadata.Author()
                    author.name = e.asString
                    model.metadata.authors.add(author)
                }
            }
        }

        if (infoObj.has("extra_animation_names") && infoObj.get("extra_animation_names").isJsonArray) {
            if (overwrite || model.properties.extraAnimations.isEmpty()) {
                model.properties.extraAnimations.clear()
                val extras = infoObj.getAsJsonArray("extra_animation_names")
                for (i in 0 until extras.size()) {
                    val extraName = extras.get(i).asString
                    model.properties.extraAnimations["extra$i"] = extraName
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun isModelFolder(dir: Path?): Boolean {
            if (dir == null || !Files.isDirectory(dir)) {
                return false
            }
            if (Files.isRegularFile(dir.resolve("ysm.json"))) {
                return true
            }
            return Files.isRegularFile(dir.resolve("main.json")) && Files.isRegularFile(dir.resolve("arm.json"))
        }

        @JvmStatic
        fun detectFormat(data: ByteArray): Int {
            if (data.size >= 2 && data[0] == 0x42.toByte() && data[1] == 0x4D.toByte()) return 1 // 'BM'
            if (data.size >= 8 && (data[0].toInt() and 0xFF) == 0x89 && data[1] == 0x50.toByte() && data[2] == 0x4E.toByte() && data[3] == 0x47.toByte()) return 2 // PNG
            if (data.size >= 2 && (data[0].toInt() and 0xFF) == 0xFF && (data[1].toInt() and 0xFF) == 0xD8) return 3 // JPEG
            if (data.size >= 12 && data[0] == 'R'.code.toByte() && data[1] == 'I'.code.toByte() && data[2] == 'F'.code.toByte() && data[3] == 'F'.code.toByte() &&
                data[8] == 'W'.code.toByte() && data[9] == 'E'.code.toByte() && data[10] == 'B'.code.toByte() && data[11] == 'P'.code.toByte()
            ) return 4 // WEBP
            if (data.size >= 12 && data[4] == 'f'.code.toByte() && data[5] == 't'.code.toByte() && data[6] == 'y'.code.toByte() && data[7] == 'p'.code.toByte()) return 5 // AVIF
            return 0
        }

        @JvmStatic
        fun getAnimTypeFromKey(key: String): Int {
            return when (key) {
                "main" -> 1
                "arm" -> 2
                "extra" -> 3
                "tac" -> 4
                "arrow" -> 5
                "carryon" -> 6
                "parcool" -> 7
                "swem" -> 8
                "slashblade" -> 9
                "tlm" -> 10
                "fp.arm", "fp_arm" -> 11
                "immersive_melodies" -> 12
                "irons_spell_books" -> 13
                else -> 0
            }
        }

        @JvmStatic
        fun getAnimKeyFromType(type: Int): String {
            return when (type) {
                1 -> "main"
                2 -> "arm"
                3 -> "extra"
                4 -> "tac"
                5 -> "arrow"
                6 -> "carryon"
                7 -> "parcool"
                8 -> "swem"
                9 -> "slashblade"
                10 -> "tlm"
                11 -> "fp_arm"
                12 -> "immersive_melodies"
                13 -> "irons_spell_books"
                else -> "unknown"
            }
        }

        @JvmStatic
        fun getStr(obj: JsonObject, key: String, def: String): String {
            return if (obj.has(key)) obj.get(key).asString else def
        }

        @JvmStatic
        fun getBool(obj: JsonObject, key: String, def: Boolean): Boolean {
            return if (obj.has(key)) obj.get(key).asBoolean else def
        }

        @JvmStatic
        fun getDouble(obj: JsonObject, key: String, def: Double): Double {
            return if (obj.has(key)) obj.get(key).asDouble else def
        }

        @JvmStatic
        fun getFloatArray(obj: JsonObject, key: String, size: Int): FloatArray {
            val result = FloatArray(size)
            if (obj.has(key)) {
                val arr = obj.getAsJsonArray(key)
                for (i in 0 until min(arr.size(), size)) {
                    result[i] = arr.get(i).asFloat
                }
            }
            return result
        }

        @JvmStatic
        fun extractFileName(fullPath: String): String {
            var name = fullPath
            val lastSlash = name.lastIndexOf('/')
            if (lastSlash >= 0) name = name.substring(lastSlash + 1)
            val dotIdx = name.lastIndexOf('.')
            if (dotIdx >= 0) name = name.substring(0, dotIdx)
            return name
        }
    }
}
