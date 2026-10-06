package com.elfmcys.yesstevemodel.resource

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.NativeLibLoader
import com.elfmcys.yesstevemodel.audio.AudioCodec
import com.elfmcys.yesstevemodel.audio.AudioTrackData
import com.elfmcys.yesstevemodel.client.ClientModelInfo
import com.elfmcys.yesstevemodel.client.gui.custom.AbstractConfig
import com.elfmcys.yesstevemodel.client.gui.custom.ExtraAnimationButtons
import com.elfmcys.yesstevemodel.client.gui.custom.configs.CheckboxConfig
import com.elfmcys.yesstevemodel.client.gui.custom.configs.RadioConfig
import com.elfmcys.yesstevemodel.client.gui.custom.configs.RangeConfig
import com.elfmcys.yesstevemodel.client.model.MainModelData
import com.elfmcys.yesstevemodel.client.texture.OuterFileTexture
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationState
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.BoneAnimation
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.BoneKeyFrame
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.BoneKeyFrameProcessor
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.EasingType
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.RawBoneKeyFrame
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.event.EventKeyFrame
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.FloatValue
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.elfmcys.yesstevemodel.geckolib3.file.*
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoBone
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache
import com.elfmcys.yesstevemodel.geckolib3.util.LinearKeyframeInterpolator
import com.elfmcys.yesstevemodel.geckolib3.util.TicksInterpolator
import com.elfmcys.yesstevemodel.model.format.ServerModelInfo
import com.elfmcys.yesstevemodel.resource.models.*
import com.elfmcys.yesstevemodel.resource.pojo.RawYsmModel
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap
import com.elfmcys.yesstevemodel.util.data.StringMapPair
import com.elfmcys.yesstevemodel.util.data.StringPair
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import org.apache.commons.lang3.tuple.Pair
import org.gagravarr.ogg.OggFile
import org.gagravarr.opus.OpusFile
import org.gagravarr.vorbis.VorbisFile
import org.joml.Vector2f
import org.joml.Vector3f
import rip.ysm.compat.oculus.ShadersTextureType
import rip.ysm.imagestream.avif.AvifDecoder
import rip.ysm.imagestream.webp.WebpDecoder
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

object YSMClientMapper {
    class TranslucencyScanner(val images: Array<BufferedImage?>, expectedCount: Int) {
        @JvmField
        val results: BooleanArray = BooleanArray(max(expectedCount, images.size))

        companion object {
            const val STATE_INVISIBLE = 0
            const val STATE_OPAQUE = 1
            const val STATE_TRANSLUCENT = 2
        }

        fun getResults(): BooleanArray = results

        fun scan(face: RawYsmModel.RawFace): Int {
            var minU = face.u[0]
            var maxU = face.u[0]
            var minV = face.v[0]
            var maxV = face.v[0]
            for (i in 1 until 4) {
                minU = min(minU, face.u[i])
                maxU = max(maxU, face.u[i])
                minV = min(minV, face.v[i])
                maxV = max(maxV, face.v[i])
            }

            var hasValidImage = false
            var faceHasVisiblePixel = false
            var faceHasTransparentPixel = false

            for (i in images.indices) {
                val img = images[i] ?: continue
                hasValidImage = true

                val imgW = img.width
                val imgH = img.height

                var startX = floor(minU * imgW + 0.01f).toInt()
                var endX = floor(maxU * imgW - 0.01f).toInt()
                if (endX < startX) endX = startX

                var startY = floor(minV * imgH + 0.01f).toInt()
                var endY = floor(maxV * imgH - 0.01f).toInt()
                if (endY < startY) endY = startY

                startX = max(0, min(startX, imgW - 1))
                endX = max(0, min(endX, imgW - 1))
                startY = max(0, min(startY, imgH - 1))
                endY = max(0, min(endY, imgH - 1))

                var imageHasVisiblePixel = false
                var imageHasTransparentPixel = false
                var imageHasColoredTranslucentPixel = false

                for (x in startX..endX) {
                    for (y in startY..endY) {
                        val alpha = (img.getRGB(x, y) ushr 24) and 0xFF

                        if (alpha > 0) {
                            imageHasVisiblePixel = true
                            if (alpha < 255) {
                                imageHasColoredTranslucentPixel = true
                            }
                        }

                        if (alpha < 255) {
                            imageHasTransparentPixel = true
                        }

                        if (imageHasVisiblePixel && imageHasTransparentPixel && imageHasColoredTranslucentPixel) {
                            break
                        }
                    }

                    if (imageHasVisiblePixel && imageHasTransparentPixel && imageHasColoredTranslucentPixel) {
                        break
                    }
                }

                if (imageHasVisiblePixel) {
                    faceHasVisiblePixel = true

                    if (imageHasTransparentPixel) {
                        faceHasTransparentPixel = true
                    }

                    if (imageHasColoredTranslucentPixel) {
                        results[i] = true
                    }
                }
            }

            if (!hasValidImage) return STATE_OPAQUE
            if (!faceHasVisiblePixel) return STATE_INVISIBLE
            if (faceHasTransparentPixel) return STATE_TRANSLUCENT
            return STATE_OPAQUE
        }
    }

    private fun decodeToImage(data: ByteArray?, imageFormat: Int, width: Int, height: Int): BufferedImage? {
        if (data == null || data.isEmpty()) {
            return null
        }

        var format = imageFormat
        if (format == 0) {
            format = YSMFolderDeserializer.detectFormat(data)
            if (format == 0) {
                format = 1
            }
        }

        return runCatching {
            when (format) {
                -1 -> {
                    if (width > 0 && height > 0 && data.size >= width * height * 4) {
                        val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                        val pixels = IntArray(width * height)
                        for (i in pixels.indices) {
                            val r = data[i * 4].toInt() and 0xFF
                            val g = data[i * 4 + 1].toInt() and 0xFF
                            val b = data[i * 4 + 2].toInt() and 0xFF
                            val a = data[i * 4 + 3].toInt() and 0xFF
                            pixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
                        }
                        img.setRGB(0, 0, width, height, pixels, 0, width)
                        img
                    } else throw RuntimeException("Invalid RGBA texture")
                }

                else -> {
                    when (format) {
                        1, 2, 3 -> ImageIO.read(ByteArrayInputStream(data))
                        4 -> WebpDecoder().read(data)
                        5 -> AvifDecoder().read(data)
                        else -> null
                    }
                }
            }
        }.onFailure {
            Constants.LOGGER.error("Failed to decode texture", it)
        }.getOrNull()
    }

    private fun encodeToPng(img: BufferedImage?, fallbackData: ByteArray?): ByteArray? {
        if (img != null) {
            runCatching {
                val baos = ByteArrayOutputStream()
                ImageIO.write(img, "png", baos)
                return baos.toByteArray()
            }.onFailure {
                Constants.LOGGER.error("Failed to encode texture", it)
            }
        }
        return fallbackData
    }

    @JvmStatic
    fun toPng(data: ByteArray?, imageFormat: Int, width: Int, height: Int): ByteArray? {
        if (imageFormat == 2) {
            return data
        }
        val img = decodeToImage(data, imageFormat, width, height)
        return encodeToPng(img, data)
    }

    @JvmStatic
    fun buildParsedBundle(raw: RawYsmModel, modelId: String): ClientModelInfo {
        val mainTextures = LinkedHashMap<String, OuterFileTexture>()
        val textureCount = max(1, raw.mainEntity.textures.size)

        val imagesList = ArrayList<BufferedImage?>()

        for (rt in raw.mainEntity.textures.values) {
            val img = decodeToImage(rt.data, rt.imageFormat, rt.width, rt.height)
            imagesList.add(img)

            val processedData = (if (rt.imageFormat == 2) rt.data else encodeToPng(img, rt.data)) ?: ByteArray(0)
            val tex = OuterFileTexture(processedData)

            val suffixTextures = LinkedHashMap<ShadersTextureType, OuterFileTexture>()
            for (sub in rt.subTextures) {
                if (sub.data == null) continue
                val processedSubData = toPng(sub.data, sub.imageFormat, sub.width, sub.height) ?: ByteArray(0)
                when (sub.specularType) {
                    1 -> {
                        suffixTextures[ShadersTextureType.NORMAL] = OuterFileTexture(processedSubData)
                    }

                    2 -> {
                        suffixTextures[ShadersTextureType.SPECULAR] = OuterFileTexture(processedSubData)
                    }
                }
            }
            tex.setSuffixTextures(suffixTextures)
            mainTextures[rt.name ?: ""] = tex
        }

        val avatarTextures = LinkedHashMap<String, OuterFileTexture>()
        for (author in raw.metadata.authors) {
            val avatarImg = author.avatarImage ?: continue
            val processedAvatarData =
                toPng(avatarImg.data, avatarImg.format, avatarImg.width, avatarImg.height) ?: ByteArray(0)
            val tex = OuterFileTexture(processedAvatarData)
            avatarTextures[avatarImg.name ?: ""] = tex
        }
        val textureMap = buildTextureMap(mainTextures)

        val context = buildContext(raw.mainEntity.mainModel)

        val imagesArray = imagesList.toTypedArray()
        val mainScanner = if (raw.mainEntity.mainModel != null) TranslucencyScanner(imagesArray, textureCount) else null
        val armScanner = if (raw.mainEntity.armModel != null) TranslucencyScanner(imagesArray, textureCount) else null

        val mainMesh = buildMesh(raw.mainEntity.mainModel, context, textureCount, mainScanner, raw.properties.allCutout)
        val armMesh = if (raw.mainEntity.armModel != null) buildMesh(
            raw.mainEntity.armModel,
            context,
            textureCount,
            armScanner,
            raw.properties.allCutout
        ) else mainMesh

        val meshes = arrayOf(mainMesh, armMesh)

        val animations = LinkedHashMap<String, AnimationFile>()
        for ((key, value) in raw.mainEntity.animationFiles) {
            animations[key] = AnimationFile(buildAnimations(value, raw.properties.mergeMultilineExpr))
        }

        val controllersList = ArrayList<AnimationControllerFile>()
        for (file in raw.mainEntity.animationControllerFiles) {
            val controllerMap = buildControllers(file.controllers, raw.properties.mergeMultilineExpr)
            if (controllerMap.isNotEmpty()) {
                controllersList.add(AnimationControllerFile(controllerMap))
            }
        }

        val mainModelData = MainModelData(meshes, animations, controllersList.toTypedArray(), textureMap)

        val modelInfo = buildModelInfo(raw)
        val extraResources = buildExtraResources(raw)
        val extraItemModels = buildExtraItemModels(raw, context, raw.properties.mergeMultilineExpr)
        val extraEntityModels = buildExtraEntityModels(raw, context, raw.properties.mergeMultilineExpr)
        val extraTextures = buildExtraTextures(raw)

        return ClientModelInfo(
            mainModelData,
            extraItemModels,
            extraEntityModels,
            extraResources,
            modelInfo,
            avatarTextures,
            extraTextures
        )
    }

    private fun buildMesh(
        rawGeo: RawYsmModel.RawGeometry?,
        context: GeometryDescription,
        textureCount: Int,
        scanner: TranslucencyScanner?,
        allCutout: Boolean
    ): GeoModel {
        if (rawGeo == null || rawGeo.bones.isEmpty()) {
            val fallbackArray = scanner?.getResults() ?: BooleanArray(max(1, textureCount))
            return buildMesh(emptyArray(), emptyMap(), context, fallbackArray)
        }

        val geoBones = ArrayList<GeoBone>()
        val bakedBones = ArrayList<GeoModel.BakedBone>()
        val parentMap = HashMap<String, String>()

        for (rb in rawGeo.bones) {
            val boneName = rb.name ?: ""
            parentMap[boneName] = rb.parentName ?: ""
            geoBones.add(
                GeoBone(
                    boneName,
                    false,
                    false,
                    false,
                    rb.pivot[0],
                    rb.pivot[1],
                    rb.pivot[2],
                    rb.rotation[0],
                    rb.rotation[1],
                    rb.rotation[2]
                )
            )

            val bb = GeoModel.BakedBone()
            bb.name = boneName
            if (boneName.startsWith("ysmGlow")) bb.glow = true
            bb.pivotX = rb.pivot[0]
            bb.pivotY = rb.pivot[1]
            bb.pivotZ = rb.pivot[2]
            bb.rotX = rb.rotation[0]
            bb.rotY = rb.rotation[1]
            bb.rotZ = rb.rotation[2]
            bb.parentIdx = -1

            var forceCull = allCutout

            for (rc in rb.cubes) {
                val bc = GeoModel.BakedCube()
                var validFaceCount = 0
                var hasTranslucentFace = false

                for (rf in rc.faces) {
                    val faceState = scanner?.scan(rf) ?: TranslucencyScanner.STATE_OPAQUE

                    if (faceState == TranslucencyScanner.STATE_INVISIBLE) {
                        continue
                    }

                    if (faceState == TranslucencyScanner.STATE_TRANSLUCENT) {
                        hasTranslucentFace = true
                    }

                    if (!forceCull && isNegativeSizedFace(rf)) {
                        forceCull = true
                    }

                    val bq = GeoModel.BakedQuad()
                    bq.normal = Vector3f(rf.normal[0], rf.normal[1], rf.normal[2])
                    bq.positions = Array(4) { i ->
                        val px = rf.positions[i][0]
                        val py = rf.positions[i][1]
                        val pz = rf.positions[i][2]
                        Vector3f(px, py, pz)
                    }
                    bq.uvs = Array(4) { i ->
                        Vector2f(rf.u[i], rf.v[i])
                    }
                    bc.quads.add(bq)
                    validFaceCount++
                }

                var isZeroThickness = true
                if (bc.quads.isNotEmpty()) {
                    val baseNormal = bc.quads[0].normal
                    val basePos = bc.quads[0].positions[0]

                    for (q in bc.quads) {
                        for (i in 0 until 4) {
                            val pos = q.positions[i]
                            val dx = pos.x - basePos.x
                            val dy = pos.y - basePos.y
                            val dz = pos.z - basePos.z

                            val distance = dx * baseNormal.x + dy * baseNormal.y + dz * baseNormal.z

                            if (abs(distance) > 1e-3f) {
                                isZeroThickness = false
                                break
                            }
                        }
                        if (!isZeroThickness) break
                    }
                } else {
                    isZeroThickness = false
                }

                when {
                    forceCull -> {
                        bc.cullable = true
                    }

                    hasTranslucentFace -> {
                        bc.cullable = false
                    }

                    isZeroThickness && validFaceCount > 1 -> {
                        bc.cullable = true
                    }

                    else -> {
                        bc.cullable = validFaceCount >= 5
                    }
                }

                if (bc.quads.isNotEmpty()) {
                    bb.cubes.add(bc)
                }
            }
            bakedBones.add(bb)
        }

        // 回填父级索引
        for (b in bakedBones) {
            val parentName = parentMap[b.name]
            if (!parentName.isNullOrEmpty()) {
                for (i in bakedBones.indices) {
                    if (bakedBones[i].name == parentName) {
                        b.parentIdx = i
                        break
                    }
                }
            }
            when {
                b.name == "LeftArm" -> b.partMask = 1
                b.name == "RightArm" -> b.partMask = 2
                b.name == "Background" -> b.partMask = 3
                b.parentIdx != -1 -> b.partMask = bakedBones[b.parentIdx].partMask
                else -> b.partMask = 0
            }
        }

        val translucencyArray = scanner?.getResults() ?: BooleanArray(max(1, textureCount))
        val mesh = buildMesh(geoBones.toTypedArray(), parentMap, context, translucencyArray)

        mesh.bakedBones = bakedBones
        if (NativeLibLoader.isLoaded()) mesh.buildNativeCache()
        return mesh
    }

    private fun buildAnimations(
        animFile: RawYsmModel.RawAnimationFile,
        mergeMultilineExpr: Boolean
    ): LinkedHashMap<String, Animation> {
        val result = LinkedHashMap<String, Animation>()
        for (ra in animFile.animations.values) {
            val loopMode = when (ra.loopMode) {
                1 -> ILoopType.EDefaultLoopTypes.LOOP
                3 -> ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME
                else -> ILoopType.EDefaultLoopTypes.PLAY_ONCE
            }

            val boneAnims = ArrayList<BoneAnimation>()
            for (rba in ra.boneAnimations) {
                val rotFrames = parseKeyframes(rba.rotation, true)
                val posFrames = parseKeyframes(rba.position, false)
                val scaleFrames = parseKeyframes(rba.scale, false)
                boneAnims.add(
                    BoneAnimation(
                        rba.boneName ?: "",
                        rotFrames.toMutableList(),
                        posFrames.toMutableList(),
                        scaleFrames.toMutableList()
                    )
                )
            }

            val soundEffects = ArrayList<EventKeyFrame<String>>()
            for (rse in ra.soundEffects) {
                soundEffects.add(EventKeyFrame((rse.timestamp * 20.0f).toDouble(), rse.effectName ?: ""))
            }

            val timelineEvents = ArrayList<EventKeyFrame<Array<IValue>>>()
            for (rte in ra.timelineEvents) {
                val values = parse(rte.events, mergeMultilineExpr)
                timelineEvents.add(EventKeyFrame((rte.timestamp * 20.0f).toDouble(), values.toTypedArray()))
            }

            val blendWeight = when (val bw = ra.blendWeight) {
                is Float -> FloatValue(bw)
                is Number -> FloatValue(bw.toFloat())
                is String -> runCatching { parse(bw) }.getOrNull()

                else -> null
            }

            val anim = Animation(
                ra.name ?: "",
                (ra.length * 20.0f).toDouble(),
                loopMode,
                blendWeight = blendWeight,
                boneAnimations = boneAnims.toTypedArray(),
                soundKeyFrames = soundEffects.toTypedArray(),
                particleKeyFrames = emptyArray(),
                customInstructionKeyframes = timelineEvents.toTypedArray()
            )
            result[ra.name ?: ""] = anim
        }
        return result
    }

    private fun parseKeyframes(frames: List<RawYsmModel.RawKeyframe>, isRotation: Boolean): List<BoneKeyFrame> {
        val builders = ArrayList<RawBoneKeyFrame>()
        for (rk in frames) {
            val builder = RawBoneKeyFrame()
            builder.startTick = (rk.timestamp * 20.0f).toDouble()
            builder.easingType = if (rk.interpolationMode == 2) EasingType.CATMULLROM else EasingType.LINEAR
            builder.contiguous = !rk.hasPreData

            if (rk.hasPreData) {
                assignToBuilder(builder, rk.preData, true)
                assignToBuilder(builder, rk.postData, false)
            } else {
                assignToBuilder(builder, rk.postData, true)
            }
            builders.add(builder)
        }
        return BoneKeyFrameProcessor.process(builders, isRotation)
    }

    private fun assignToBuilder(builder: RawBoneKeyFrame, data: Array<Any?>, isPre: Boolean) {
        for (axis in 0 until 3) {
            var dVal = 0.0
            var iVal: IValue? = null
            when (val valObj = data[axis]) {
                is Float -> dVal = valObj.toDouble()
                is Number -> dVal = valObj.toDouble()
                is String -> runCatching { iVal = parse(valObj) }
            }
            when {
                isPre -> {
                    when (axis) {
                        0 -> {
                            builder.preX = dVal
                            builder.preXValue = iVal
                        }

                        1 -> {
                            builder.preY = dVal
                            builder.preYValue = iVal
                        }

                        2 -> {
                            builder.preZ = dVal
                            builder.preZValue = iVal
                        }
                    }
                }

                else -> {
                    when (axis) {
                        0 -> {
                            builder.postX = dVal
                            builder.postXValue = iVal
                        }

                        1 -> {
                            builder.postY = dVal
                            builder.postYValue = iVal
                        }

                        2 -> {
                            builder.postZ = dVal
                            builder.postZValue = iVal
                        }
                    }
                }
            }
        }
    }

    private fun buildControllers(
        rawControllers: Map<String, RawYsmModel.RawAnimationController>,
        mergeMultilineExpr: Boolean
    ): LinkedHashMap<String, AnimationController> {
        val result = LinkedHashMap<String, AnimationController>()
        for (rac in rawControllers.values) {
            val states = ArrayList<AnimationState>()
            for (rs in rac.states) {
                val animations = ArrayList<Pair<String, IValue>>()
                for ((k, v) in rs.animations) {
                    var blend: IValue? = null
                    if (v.isNotEmpty()) runCatching { blend = parse(v) }
                    animations.add(Pair.of(k, blend))
                }

                val transitions = ArrayList<Pair<String, IValue>>()
                for ((k, v) in rs.transitions) {
                    val condition = parse(v)
                    transitions.add(Pair.of(k, condition))
                }

                val onEntry = parse(rs.onEntry, mergeMultilineExpr)
                val onExit = parse(rs.onExit, mergeMultilineExpr)

                val blendTransition = if (rs.blendTransitions.isNotEmpty()) {
                    val keys = FloatArray(rs.blendTransitions.size)
                    val values = FloatArray(rs.blendTransitions.size)
                    var i = 0
                    for ((k, v) in rs.blendTransitions) {
                        keys[i] = k
                        values[i] = v
                        i++
                    }
                    LinearKeyframeInterpolator(keys, values)
                } else {
                    TicksInterpolator(rs.blendTransitionValue)
                }

                states.add(
                    AnimationState(
                        rs.name ?: "",
                        animations.toTypedArray(),
                        transitions.toTypedArray(),
                        rs.soundEffects.toTypedArray(),
                        onEntry.toTypedArray(),
                        onExit.toTypedArray(),
                        blendTransition,
                        rs.blendViaShortestPath
                    )
                )
            }
            result[rac.animationName ?: ""] = AnimationController(
                if (rac.initialState.isNullOrEmpty()) "default" else rac.initialState!!,
                states.toTypedArray()
            )
        }
        return result
    }

    @JvmStatic
    fun buildModelInfo(raw: RawYsmModel): ServerModelInfo {
        val rm = raw.metadata
        val authors = ArrayList<AuthorInfo>()
        for (a in rm.authors) {
            authors.add(AuthorInfo(a.name, a.role, OrderedStringMap(Object2ObjectArrayMap(a.contacts)), a.comment))
        }

        val extraInfo = Metadata(
            rm.name,
            rm.tips,
            StringPair(rm.licenseType, rm.licenseDescription),
            authors.toTypedArray(),
            OrderedStringMap(Object2ObjectArrayMap(rm.links))
        )

        val rp = raw.properties
        val classifyList = ArrayList<StringMapPair>()
        for (rCls in rp.extraAnimationClassifies) {
            classifyList.add(StringMapPair(rCls.id ?: "", OrderedStringMap(Object2ObjectArrayMap(rCls.extras))))
        }

        val buttonsList = ArrayList<ExtraAnimationButtons>()
        for (rBtn in rp.extraAnimationButtons) {
            val metaList = ArrayList<AbstractConfig>()
            for (form in rBtn.forms) {
                when (form.type) {
                    "checkbox" -> metaList.add(
                        CheckboxConfig(
                            form.title ?: "",
                            form.description ?: "",
                            form.defaultValue ?: ""
                        )
                    )

                    "radio" -> metaList.add(
                        RadioConfig(
                            form.title ?: "",
                            form.description ?: "",
                            form.defaultValue ?: "",
                            OrderedStringMap(Object2ObjectArrayMap(form.labels))
                        )
                    )

                    "range" -> metaList.add(
                        RangeConfig(
                            form.title ?: "",
                            form.description ?: "",
                            form.defaultValue ?: "",
                            form.step.toDouble(),
                            form.min.toDouble(),
                            form.max.toDouble()
                        )
                    )
                }
            }
            buttonsList.add(
                ExtraAnimationButtons(
                    rBtn.id ?: "",
                    rBtn.name ?: "",
                    rBtn.description ?: "",
                    metaList.toTypedArray()
                )
            )
        }
        val properties = ModelProperties(
            rp.heightScale,
            rp.widthScale,
            rp.defaultTexture,
            rp.previewAnimation,
            OrderedStringMap(Object2ObjectArrayMap(rp.extraAnimations)),
            buttonsList.toTypedArray(),
            classifyList.toTypedArray(),
            rp.isFree,
            rp.renderLayersFirst,
            rp.disablePreviewRotation
        )

        var bones = 0
        var cubes = 0
        var faces = 0
        val mainModel = raw.mainEntity.mainModel
        if (mainModel != null) {
            bones = mainModel.bones.size
            for (bone in mainModel.bones) {
                cubes += bone.cubes.size
                for (cube in bone.cubes) {
                    faces += cube.faces.size
                }
            }
        }
        val stats = MainModelInfo(bones, cubes, faces)

        val footer = raw.footer
        return ServerModelInfo(
            extraInfo,
            properties,
            stats,
            footer.version,
            rp.sha256,
            footer.extra,
            footer.time,
            footer.rand
        )
    }

    private fun buildExtraResources(raw: RawYsmModel): ModelExtraResourcesFile {
        val sounds = LinkedHashMap<String, AudioTrackData>()
        for ((name, value) in raw.soundFiles) {
            val track = parseAudioTrackData(value.data)
            if (track != null) sounds[name] = track
        }

        val functions = LinkedHashMap<String, IValue>()
        for ((name, value) in raw.functionFiles) {
            val data = value.data ?: continue
            val molangScript = String(data, StandardCharsets.UTF_8)
            runCatching { functions[name] = GeckoLibCache.getMolangParser().parseExpression(molangScript, true) }
        }

        val translations = LinkedHashMap<String, MutableMap<String, String>>()
        for ((key, value) in raw.languageFiles) {
            val data = value.data
            translations[key] = data
            val normalized = key.lowercase(java.util.Locale.ROOT).replace('-', '_')
            if (normalized != key) {
                translations[normalized] = data
            }
        }

        return ModelExtraResourcesFile(sounds, functions, translations)
    }

    private fun parseAudioTrackData(oggData: ByteArray?): AudioTrackData? {
        if (oggData == null || oggData.size < 8) return null
        return runCatching {
            val bais = ByteArrayInputStream(oggData)
            val oggFile = OggFile(bais)
            val header = String(oggData, 0, min(oggData.size, 100), StandardCharsets.US_ASCII)
            val isOpus = header.contains("OpusHead")

            val codec = if (isOpus) AudioCodec.OPUS else AudioCodec.VORBIS
            val sampleRate = if (isOpus) {
                val opus = OpusFile(oggFile)
                opus.info.rate.toInt()
            } else {
                val vorbis = VorbisFile(oggFile)
                vorbis.info.rate.toInt()
            }

            val reader = oggFile.packetReader
            var durationSamples = 0L
            var packet = reader.nextPacket
            while (packet != null) {
                val granule = packet.granulePosition
                if (granule > 0) durationSamples = granule
                packet = reader.nextPacket
            }

            val directBuf = ByteBuffer.allocateDirect(oggData.size)
            directBuf.put(oggData)
            directBuf.flip()

            AudioTrackData(directBuf, codec.ordinal, sampleRate, durationSamples)
        }.getOrNull()
    }

    private fun buildExtraItemModels(
        raw: RawYsmModel,
        context: GeometryDescription,
        mergeMultilineExpr: Boolean
    ): Array<ProjectileModelFiles> {
        val list = ArrayList<ProjectileModelFiles>()
        for (sub in raw.projectiles.values) {
            val holder = buildSubEntityHolder(sub, context, 1, mergeMultilineExpr)
            list.add(holder)
        }
        return list.toTypedArray()
    }

    private fun buildExtraEntityModels(
        raw: RawYsmModel,
        context: GeometryDescription,
        mergeMultilineExpr: Boolean
    ): Array<VehicleModelFiles> {
        val list = ArrayList<VehicleModelFiles>()
        for (sub in raw.vehicles.values) {
            val wrapper = buildSubEntityWrapper(sub, context, 1, mergeMultilineExpr)
            list.add(wrapper)
        }
        return list.toTypedArray()
    }

    private fun buildSubEntityHolder(
        sub: RawYsmModel.RawSubEntity,
        context: GeometryDescription,
        textureCount: Int,
        mergeMultilineExpr: Boolean
    ): ProjectileModelFiles {
        var texture: OuterFileTexture? = null
        var subScanner: TranslucencyScanner? = null

        if (sub.textures.isNotEmpty()) {
            val imgList = ArrayList<BufferedImage?>()
            for (rt in sub.textures.values) {
                val img = decodeToImage(rt.data, rt.imageFormat, rt.width, rt.height)
                imgList.add(img)
                val processedData = (if (rt.imageFormat == 2) rt.data else encodeToPng(img, rt.data)) ?: ByteArray(0)
                if (texture == null) {
                    texture = OuterFileTexture(processedData)
                }
            }
            if (sub.model != null) {
                subScanner = TranslucencyScanner(imgList.toTypedArray(), textureCount)
            }
        }

        val mesh = buildMesh(sub.model, context, textureCount, subScanner, true)

        val allAnimations = LinkedHashMap<String, Animation>()
        for (entry in sub.animationFiles.values) {
            val fileAnims = buildAnimations(entry, mergeMultilineExpr)
            allAnimations.putAll(fileAnims)
        }
        val combinedAnim = AnimationFile(allAnimations)

        val controllerMap = LinkedHashMap<String, AnimationController>()
        for (file in sub.animationControllerFiles) {
            if (file.controllers.isNotEmpty()) {
                controllerMap.putAll(buildControllers(file.controllers, mergeMultilineExpr))
            }
        }
        val controllers = AnimationControllerFile(controllerMap)

        val matchIds = sub.matchIds ?: arrayOf(sub.identifier ?: "")
        return ProjectileModelFiles(
            matchIds,
            mesh,
            combinedAnim,
            controllers,
            texture ?: OuterFileTexture(ByteArray(0))
        )
    }

    private fun buildSubEntityWrapper(
        sub: RawYsmModel.RawSubEntity,
        context: GeometryDescription,
        textureCount: Int,
        mergeMultilineExpr: Boolean
    ): VehicleModelFiles {
        var texture: OuterFileTexture? = null
        var subScanner: TranslucencyScanner? = null

        if (sub.textures.isNotEmpty()) {
            val imgList = ArrayList<BufferedImage?>()
            for (rt in sub.textures.values) {
                val img = decodeToImage(rt.data, rt.imageFormat, rt.width, rt.height)
                imgList.add(img)
                val processedData = (if (rt.imageFormat == 2) rt.data else encodeToPng(img, rt.data)) ?: ByteArray(0)
                if (texture == null) {
                    texture = OuterFileTexture(processedData)
                }
            }
            if (sub.model != null) {
                subScanner = TranslucencyScanner(imgList.toTypedArray(), textureCount)
            }
        }

        val mesh = buildMesh(sub.model, context, textureCount, subScanner, true)

        val allAnimations = LinkedHashMap<String, Animation>()
        for (animFile in sub.animationFiles.values) {
            val fileAnims = buildAnimations(animFile, mergeMultilineExpr)
            allAnimations.putAll(fileAnims)
        }
        val combinedAnim = AnimationFile(allAnimations)

        val controllerMap = LinkedHashMap<String, AnimationController>()
        for (file in sub.animationControllerFiles) {
            if (file.controllers.isNotEmpty()) {
                controllerMap.putAll(buildControllers(file.controllers, mergeMultilineExpr))
            }
        }
        val controllers = AnimationControllerFile(controllerMap)

        val matchIds = sub.matchIds ?: arrayOf(sub.identifier ?: "")
        return VehicleModelFiles(matchIds, mesh, combinedAnim, controllers, texture ?: OuterFileTexture(ByteArray(0)))
    }

    private fun buildExtraTextures(raw: RawYsmModel): MutableMap<String, OuterFileTexture> {
        val result = LinkedHashMap<String, OuterFileTexture>()
        for (img in raw.properties.backgroundImages) {
            if (!img.name.isNullOrEmpty()) {
                val processedData = toPng(img.data, img.format, img.width, img.height) ?: ByteArray(0)
                result[img.name ?: ""] = OuterFileTexture(processedData)
            }
        }
        return result
    }

    @JvmStatic
    fun parse(array: List<String>, mergeMultilineExpr: Boolean): List<IValue> {
        val values = ArrayList<IValue>()

        if (!mergeMultilineExpr) {
            for (expr in array) values.add(parse(expr))
            return values
        }

        runCatching {
            val parserText = StringBuilder()
            for (i in array.indices) {
                parserText.append(array[i])
                if (i < array.size - 1) {
                    parserText.append("\n")
                }
            }
            values.add(parse(parserText.toString()))
        }.onFailure {
            values.add(FloatValue.ZERO)
        }
        return values
    }

    @JvmStatic
    fun parse(str: String): IValue {
        return runCatching {
            GeckoLibCache.getMolangParser().parseExpression(str, false)
        }.getOrElse {
            FloatValue.ZERO
        }
    }

    private fun buildPath(targetBone: String, parentMap: Map<String, String>): Array<String> {
        if (!parentMap.containsKey(targetBone)) return emptyArray()
        val path = ArrayList<String>()
        var current: String? = targetBone
        while (!current.isNullOrEmpty()) {
            path.add(current)
            current = parentMap[current]
        }
        path.reverse()
        return path.toTypedArray()
    }

    private fun buildBoneNameArrays(parentMap: Map<String, String>): Array<Array<String>> {
        val targetLocators = arrayOf(
            "LeftHandLocator",
            "RightHandLocator",
            "ElytraLocator",
            "PistolLocator",
            "RifleLocator",
            "LeftWaistLocator",
            "RightWaistLocator",
            "LeftShoulderLocator",
            "RightShoulderLocator",
            "BladeLocator",
            "SheathLocator",
            "Head",
            "BackpackLocator",
            "LeftHandLocator2",
            "LeftHandLocator3",
            "LeftHandLocator4",
            "LeftHandLocator5",
            "LeftHandLocator6",
            "LeftHandLocator7",
            "LeftHandLocator8",
            "RightHandLocator2",
            "RightHandLocator3",
            "RightHandLocator4",
            "RightHandLocator5",
            "RightHandLocator6",
            "RightHandLocator7",
            "RightHandLocator8",
            "PassengerLocator",
            "PassengerLocator2",
            "PassengerLocator3",
            "PassengerLocator4",
            "PassengerLocator5",
            "PassengerLocator6",
            "PassengerLocator7",
            "PassengerLocator8"
        )

        val arrays = Array(35) { i ->
            val locator = targetLocators[i]
            if (locator.isNotEmpty()) {
                buildPath(locator, parentMap)
            } else {
                emptyArray()
            }
        }

        return arrays
    }

    @JvmStatic
    fun buildMesh(
        bones: Array<GeoBone>,
        parentMap: Map<String, String>,
        context: GeometryDescription,
        translucencyArray: BooleanArray
    ): GeoModel {
        val boneNameArrays = buildBoneNameArrays(parentMap)
        val flags = booleanArrayOf(
            parentMap.containsKey("LeftArm"),
            parentMap.containsKey("RightArm"),
            parentMap.containsKey("Background")
        )
        return GeoModel(bones, boneNameArrays, flags, context, translucencyArray)
    }

    @JvmStatic
    fun buildTextureMap(textures: Map<String, OuterFileTexture>): OrderedStringMap<String, OuterFileTexture> {
        if (textures.isEmpty()) {
            return OrderedStringMap(emptyArray(), emptyArray())
        }
        val keys = textures.keys.toTypedArray()
        val values = textures.values.toTypedArray()
        return OrderedStringMap(keys, values)
    }

    @JvmStatic
    fun buildContext(model: RawYsmModel.RawGeometry?): GeometryDescription {
        if (model == null) {
            return GeometryDescription("", 64.0, 64.0, 0.0, 0.0, DoubleArray(0))
        }
        val offset = model.visibleBoundsOffset
        val offsetArray = if (offset != null) {
            DoubleArray(offset.size) { offset[it].toDouble() }
        } else {
            DoubleArray(0)
        }
        return GeometryDescription(
            model.identifier,
            model.textureWidth.toDouble(),
            model.textureHeight.toDouble(),
            model.visibleBoundsWidth.toDouble(),
            model.visibleBoundsHeight.toDouble(),
            offsetArray
        )
    }

    private fun isNegativeSizedFace(f: RawYsmModel.RawFace): Boolean {
        val p0 = f.positions[0]
        val p1 = f.positions[1]
        val p2 = f.positions[2]

        val ax = p1[0] - p0[0]
        val ay = p1[1] - p0[1]
        val az = p1[2] - p0[2]

        var bx = p2[0] - p0[0]
        var by = p2[1] - p0[1]
        var bz = p2[2] - p0[2]

        var nx = ay * bz - az * by
        var ny = az * bx - ax * bz
        var nz = ax * by - ay * bx

        var len2 = nx * nx + ny * ny + nz * nz
        if (len2 <= 1e-10f) {
            val p3 = f.positions[3]

            bx = p3[0] - p0[0]
            by = p3[1] - p0[1]
            bz = p3[2] - p0[2]

            nx = ay * bz - az * by
            ny = az * bx - ax * bz
            nz = ax * by - ay * bx

            len2 = nx * nx + ny * ny + nz * nz
            if (len2 <= 1e-10f) {
                return false
            }
        }
        val dot = nx * f.normal[0] + ny * f.normal[1] + nz * f.normal[2]
        return dot < 0.0f
    }
}
