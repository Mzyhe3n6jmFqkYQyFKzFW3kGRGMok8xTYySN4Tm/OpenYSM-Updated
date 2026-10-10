@file:Suppress("unused")

package com.elfmcys.yesstevemodel.resource

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.resource.pojo.RawYsmModel
import io.netty.buffer.Unpooled
import rip.ysm.security.YSMByteBuf

class YSMBinaryDeserializer : AutoCloseable {
    val reader: YSMByteBuf
    val format: Int
    val model: RawYsmModel

    constructor(decompressedData: ByteArray) {
        reader = YSMByteBuf(Unpooled.wrappedBuffer(decompressedData))
        format = reader.readDword().toInt()
        model = RawYsmModel().apply { formatVersion = format }
    }

    constructor(decompressedData: ByteArray, format: Int) {
        reader = YSMByteBuf(Unpooled.wrappedBuffer(decompressedData))
        this.format = format
        model = RawYsmModel().apply { formatVersion = format }
    }

    private fun deserializeInternal(closeOnExit: Boolean): RawYsmModel {
        Constants.LOGGER.info("deserializing format $format file...")
        when {
            format < 4 -> deserializeLegacyV1()
            format <= 15 -> deserializeLegacyV15()
            else -> deserializeModern()
        }

        model.projectiles.values.removeIf {
            it.model == null || it.textures.isEmpty()
        }

        val offset = reader.offset
        if (closeOnExit) reader.close()
        Constants.LOGGER.info("end offset: 0x" + Integer.toHexString(offset))
        return model
    }

    fun deserialize(): RawYsmModel = deserializeInternal(true)

    fun deserializeKeepOpen(): RawYsmModel = deserializeInternal(false)

    fun parseYSMFooter(footer: RawYsmModel) {
        runCatching {
            if (format < 9) return
            if (format > 26) {
                model.footer.version = reader.readVarInt()
            } else {
                model.footer.version = format
            }

            model.footer.unkInt1 = reader.readVarInt()
            if (model.footer.unkInt1 != 0) {
                model.footer.rand = reader.readString()
            }
            model.footer.time = reader.readVarLong()
            if (model.footer.unkInt1 != 0) {
                model.footer.extra = reader.readString()
                if (format >= 24) {
                    model.footer.unkInt2 = reader.readVarInt()
                }
            }
        }.onFailure {
            Constants.LOGGER.error("Failed to parse YSM footer", it)
        }
    }

    private fun deserializeLegacyV1() {
        val unknownNeedSkipBytes = reader.readVarInt()
        reader.skipBytes(unknownNeedSkipBytes)

        val tempModels = ArrayList<RawYsmModel.RawGeometry>()
        val modelCount = reader.readVarInt()
        for (i in 0 until modelCount) {
            val modelId = reader.readVarInt()
            val unknownMustBeOneFlag = reader.readVarInt()
            if (unknownMustBeOneFlag != 1) throw RuntimeException("Expected 1")
            val rawGeometry = parseModels()
            rawGeometry.modelType = modelId
            tempModels.add(rawGeometry)
        }
        assignMainModels(tempModels)

        val tempAnims = HashMap<Int, RawYsmModel.RawAnimationFile>()
        val animationBlobCount = reader.readVarInt()
        for (i in 0 until animationBlobCount) {
            val animationId = reader.readVarInt()
            val unknownPadding = reader.readVarInt()
            if (unknownPadding != 1) throw RuntimeException("Expected 1")
            val rawAnimationFile = parseAnimations()

            val animKey = YSMFolderDeserializer.getAnimKeyFromType(animationId)
            if (animationId == 5) {
                val arrowEntity = model.projectiles.computeIfAbsent("minecraft:arrow") { k ->
                    RawYsmModel.RawSubEntity().apply { identifier = k }
                }
                arrowEntity.animationFiles[animKey] = rawAnimationFile
            } else {
                model.mainEntity.animationFiles[animKey] = rawAnimationFile
            }

            rawAnimationFile.animType = animationId
            tempAnims[animationId] = rawAnimationFile
        }

        val tempTextures = ArrayList<RawYsmModel.RawTexture>()
        val customTextureCount = reader.readVarInt()
        for (i in 0 until customTextureCount) {
            val tex = RawYsmModel.RawTexture()
            tex.name = reader.readString()
            if (format < 4) {
                val unknownFormatFlag = reader.readVarInt()
                if (unknownFormatFlag != 0x01) throw RuntimeException("Expected 0x01")
            }
            tex.data = reader.readByteArray()
            tex.width = reader.readVarInt()
            tex.height = reader.readVarInt()
            tex.imageFormat = -1 // RGBA

            if ("arrow.png" == tex.name) {
                val arrow = model.projectiles.computeIfAbsent("minecraft:arrow") { k ->
                    RawYsmModel.RawSubEntity().apply { identifier = k }
                }
                arrow.textures[tex.name ?: ""] = tex
            } else {
                model.mainEntity.textures[tex.name ?: ""] = tex
            }

            tempTextures.add(tex)
        }

        // Tables 回填 Hash
        val modelTableSize = reader.readVarInt()
        for (i in 0 until modelTableSize) {
            val modelId = reader.readVarInt()
            val modelHash = reader.readString()
            for (tempModel in tempModels) {
                if (tempModel.modelType == modelId) {
                    tempModel.sha256 = modelHash
                }
            }
        }

        val animationTableSize = reader.readVarInt()
        for (i in 0 until animationTableSize) {
            val animationId = reader.readVarInt()
            tempAnims[animationId]?.fileHash = reader.readString()
        }

        val textureTableSize = reader.readVarInt()
        for (i in 0 until textureTableSize) {
            val textureName = reader.readString()
            val textureHash = reader.readString()
            val tex = model.mainEntity.textures[textureName]
            if (tex != null) tex.hash = textureHash
        }

        val unkString = reader.readString()
        model.properties.sha256 = unkString
    }

    private fun deserializeLegacyV15() {
        val unknownNeedSkipBytes = reader.readVarInt()
        reader.skipBytes(unknownNeedSkipBytes)

        val tempModels = ArrayList<RawYsmModel.RawGeometry>()
        val modelCount = reader.readVarInt()
        for (i in 0 until modelCount) {
            val modelId = reader.readVarInt()
            val unknownPadding = reader.readVarInt()
            if (unknownPadding != 1) throw RuntimeException("Expected 1")
            val rawGeometry = parseModels()
            rawGeometry.modelType = modelId
            tempModels.add(rawGeometry)
        }

        assignMainModels(tempModels)

        val tempAnims = HashMap<Int, RawYsmModel.RawAnimationFile>()
        val animationBlobCount = reader.readVarInt()
        for (i in 0 until animationBlobCount) {
            val animationId = reader.readVarInt()
            val unknownPadding = reader.readVarInt()
            if (unknownPadding != 1) throw RuntimeException("Expected 1")
            val rawAnimationFile = parseAnimations()

            val animKey = YSMFolderDeserializer.getAnimKeyFromType(animationId)
            if (animationId == 5) {
                val arrowEntity = model.projectiles.computeIfAbsent("minecraft:arrow") { k ->
                    RawYsmModel.RawSubEntity().apply { identifier = k }
                }
                arrowEntity.animationFiles[animKey] = rawAnimationFile
            } else {
                model.mainEntity.animationFiles[animKey] = rawAnimationFile
            }

            rawAnimationFile.animType = animationId
            tempAnims[animationId] = rawAnimationFile
        }

        if (format > 9) {
            parseAnimationControllers(model.mainEntity.animationControllerFiles, false)
        }

        val tempTextures = ArrayList<RawYsmModel.RawTexture>()
        val customTextureCount = reader.readVarInt()
        for (i in 0 until customTextureCount) {
            val tex = RawYsmModel.RawTexture()
            tex.name = reader.readString()
            tex.data = reader.readByteArray()
            tex.width = reader.readVarInt()
            tex.height = reader.readVarInt()
            tex.imageFormat = -1

            if ("arrow.png" == tex.name) {
                val arrow = model.projectiles.computeIfAbsent("minecraft:arrow") { k ->
                    RawYsmModel.RawSubEntity().apply { identifier = k }
                }
                arrow.textures[tex.name ?: ""] = tex
            } else {
                model.mainEntity.textures[tex.name ?: ""] = tex
            }

            tempTextures.add(tex)
        }

        // Tables 回填 Hash
        val modelTableSize = reader.readVarInt()
        for (i in 0 until modelTableSize) {
            val modelId = reader.readVarInt()
            val modelHash = reader.readString()
            for (tempModel in tempModels) {
                if (tempModel.modelType == modelId) {
                    tempModel.sha256 = modelHash
                }
            }
        }

        val animationTableSize = reader.readVarInt()
        for (i in 0 until animationTableSize) {
            val animationId = reader.readVarInt()
            tempAnims[animationId]?.fileHash = reader.readString()
        }

        if (format > 9) {
            val animationControllerTableSize = reader.readVarInt()
            for (i in 0 until animationControllerTableSize) {
                val animationControllerId = reader.readVarInt()
                val hash = reader.readString()
                for (ac in model.mainEntity.animationControllerFiles) {
                    if (ac.legacyUnknownInt == animationControllerId) {
                        ac.hash = hash
                    }
                }
            }
        }

        val textureTableSize = reader.readVarInt()
        for (i in 0 until textureTableSize) {
            val textureName = reader.readString()
            val textureHash = reader.readString()
            val tex = model.mainEntity.textures[textureName]
            if (tex != null) tex.hash = textureHash
        }

        val tempAvatars = ArrayList<RawYsmModel.RawImage>()
        if (format > 9) {
            val avatarsCount = reader.readVarInt()
            for (i in 0 until avatarsCount) {
                val avatar = RawYsmModel.RawImage()
                avatar.name = reader.readString()
                avatar.data = reader.readByteArray()
                avatar.width = reader.readVarInt()
                avatar.height = reader.readVarInt()
                avatar.format = -1
                tempAvatars.add(avatar)
            }
        }

        parseYSMJson()

        for ((i, element) in tempAvatars.withIndex()) {
            val avatar = element
            if (i < model.metadata.authors.size) {
                model.metadata.authors[i].avatar = avatar.name ?: ""
                model.metadata.authors[i].avatarImage = avatar
            } else model.metadata.extraAvatars.add(avatar)
        }
    }

    @Throws(RuntimeException::class)
    private fun deserializeModern() {
        parseSoundFiles()
        parseFunctionFiles()
        parseLanguageFiles()

        if (format < 26) {
            val subEntityTotalCount = reader.readVarInt()
            for (i in 0 until subEntityTotalCount) {
                parseSubEntity(model.vehicles, "SubEntity", i)
            }
            @Suppress("UnusedVariable")
            val footerFlag = reader.readVarInt()
        } else {
            val vehiclesTotalCount = reader.readVarInt()
            for (i in 0 until vehiclesTotalCount) parseSubEntity(model.vehicles, "Vehicle", i)

            val projectilesTotalCount = reader.readVarInt()
            for (i in 0 until projectilesTotalCount) parseSubEntity(model.projectiles, "Projectile", i)
        }

        val unknownEntityFlag = reader.readVarInt()
        if (unknownEntityFlag != 1) throw RuntimeException("Expected 1 after SubEntities")

        val animationCount = reader.readVarInt()
        for (i in 0 until animationCount) {
            val type = reader.readVarInt()
            val hash = reader.readString()

            val animRef = parseAnimations()
            model.mainEntity.animationFiles[YSMFolderDeserializer.getAnimKeyFromType(type)] = animRef
            animRef.animType = type
            animRef.fileHash = hash
        }

        parseAnimationControllers(model.mainEntity.animationControllerFiles, true)
        parseTextureFiles(model.mainEntity.textures)

        val modelTotalCount = reader.readVarInt()
        val tempMainModels = ArrayList<RawYsmModel.RawGeometry>()
        for (i in 0 until modelTotalCount) {
            val modelType = reader.readVarInt()
            val hash = reader.readString()

            val geoRef = parseModels()
            geoRef.sha256 = hash
            geoRef.modelType = modelType
            tempMainModels.add(geoRef)
            Constants.LOGGER.info("Model Table Entry: ID=$modelType, Hash=$hash")
        }
        assignMainModels(tempMainModels)

        parseYSMJson()
    }

    private fun parseSubEntity(
        targetMap: MutableMap<String, RawYsmModel.RawSubEntity>,
        categoryName: String,
        index: Int
    ) {
        val subEntity = RawYsmModel.RawSubEntity()
        val subModuleName: String
        if (format <= 26) {
            subModuleName = reader.readString()
            subEntity.identifier = subModuleName
        } else {
            subEntity.identifier = "${categoryName}_$index"
        }
        val animationCount = reader.readVarInt()
        for (i in 0 until animationCount) {
            val hash = reader.readString()
            val rawAnimationFile = parseAnimations()
            subEntity.animationFiles[categoryName] = rawAnimationFile
            rawAnimationFile.fileHash = hash
        }

        parseAnimationControllers(subEntity.animationControllerFiles, false)

        val baseTex = RawYsmModel.RawTexture()
        val imgRes = parseSpecialImage()
        baseTex.hash = imgRes.hash
        baseTex.data = imgRes.data
        baseTex.width = reader.readVarInt()
        baseTex.height = reader.readVarInt()
        baseTex.imageFormat = reader.readVarInt()
        baseTex.unknownFlag = reader.readVarInt()
        baseTex.name = "base_texture_$index"
        subEntity.textures[baseTex.name ?: ""] = baseTex

        // Sub Textures
        val subTextureSize = reader.readVarInt()
        for (i in 0 until subTextureSize) {
            val subTex = RawYsmModel.RawTexture.SubTexture()
            subTex.specularType = reader.readVarInt()
            val specRes = parseSpecialImage()
            subTex.hash = specRes.hash
            subTex.data = specRes.data
            subTex.width = reader.readVarInt()
            subTex.height = reader.readVarInt()
            subTex.imageFormat = reader.readVarInt()
            subTex.unknownFlag = reader.readVarInt()
            baseTex.subTextures.add(subTex)
        }

        val modelHash = reader.readString()
        subEntity.model = parseModels()
        subEntity.model?.sha256 = modelHash

        if (format > 26) {
            val footerFlag = reader.readVarInt()
            val footerSubModuleName = reader.readString()
            subEntity.identifier = footerSubModuleName
        }

        targetMap[subEntity.identifier ?: ""] = subEntity
    }

    private fun parseModels(): RawYsmModel.RawGeometry {
        val geo = RawYsmModel.RawGeometry()

        val boneCount = reader.readVarInt()
        for (i in 0 until boneCount) {
            val bone = RawYsmModel.RawBone()
            bone.parentName = reader.readString()
            val cubeCount = reader.readVarInt()

            for (j in 0 until cubeCount) {
                val cube = RawYsmModel.RawCube()
                val faceCount = reader.readVarInt()
                for (k in 0 until faceCount) {
                    val face = RawYsmModel.RawFace()
                    face.normal = readVector3D()
                    for (v in 0 until 4) {
                        face.positions[v] = readVector3D()
                        face.u[v] = reader.readFloat()
                        face.v[v] = reader.readFloat()
                    }
                    cube.faces.add(face)
                }
                cube.unkInt1 = reader.readVarInt()
                cube.unkInt2 = reader.readVarInt()
                cube.unkInt3 = reader.readVarInt()
                bone.cubes.add(cube)
            }

            bone.name = reader.readString()
            bone.unkPad1 = reader.readVarInt()
            bone.unkPad2 = reader.readVarInt()
            bone.unkPad3 = reader.readVarInt()
            bone.unkPad4 = reader.readVarInt()
            bone.unkPad5 = reader.readVarInt()

            bone.pivot = readVector3D()
            bone.rotation = readVector3D()
            geo.bones.add(bone)
        }

        geo.identifier = reader.readString()
        geo.textureHeight = reader.readFloat()
        geo.textureWidth = reader.readFloat()
        geo.visibleBoundsHeight = reader.readFloat()
        geo.visibleBoundsWidth = reader.readFloat()

        val visibleBoundsOffsetSize = reader.readVarInt()
        geo.visibleBoundsOffset = FloatArray(visibleBoundsOffsetSize) { reader.readFloat() }

        geo.unkFloat1 = reader.readFloat()
        geo.unkFloat2 = reader.readFloat()

        val hasInfoJsonFlag = reader.readVarInt()
        if (hasInfoJsonFlag > 0) {
            parseLegacyYSMInfo()
        }

        geo.footerPad1 = reader.readVarInt()
        geo.footerPad2 = reader.readVarInt()
        geo.footerPad3 = reader.readVarInt()

        return geo
    }

    private fun parseYSMJson() {
        model.properties.sha256 = reader.readString()
        val isNewVersionYsm = reader.readVarInt()

        if (isNewVersionYsm != 0) {
            if (format <= 15) {
                reader.readVarInt() // unknown
            }

            model.metadata.name = reader.readString()
            model.metadata.tips = reader.readString()
            model.metadata.licenseType = reader.readString()
            model.metadata.licenseDescription = reader.readString()

            val authorsCount = reader.readVarInt()
            for (i in 0 until authorsCount) {
                val author = RawYsmModel.RawMetadata.Author()
                author.name = reader.readString()
                author.role = reader.readString()
                val contactsCount = reader.readVarInt()
                for (j in 0 until contactsCount) {
                    author.contacts[reader.readString()] = reader.readString()
                }
                author.comment = reader.readString()
                model.metadata.authors.add(author)
            }

            val linksCount = reader.readVarInt()
            for (i in 0 until linksCount) {
                model.metadata.links[reader.readString()] = reader.readString()
            }
        }

        if (isNewVersionYsm == 0 && format <= 15) return

        model.properties.widthScale = reader.readFloat()
        model.properties.heightScale = reader.readFloat()

        val extraAnimationsCount = reader.readVarInt()
        for (i in 0 until extraAnimationsCount) {
            try {
                model.properties.extraAnimations[reader.readString()] = reader.readString()
            } catch (ex: Throwable) {
                throw RuntimeException("Error reading extra animations at index $i", ex)
            }
        }

        if (format > 9) {
            val extraAnimationButtonsCount = reader.readVarInt()
            for (i in 0 until extraAnimationButtonsCount) {
                val btn = RawYsmModel.ExtraAnimationButton()
                btn.id = reader.readString()
                btn.name = reader.readString()
                reader.readVarInt() // buttonPadding

                val configurationFormsCount = reader.readVarInt()
                for (j in 0 until configurationFormsCount) {
                    val form = RawYsmModel.ConfigForm()
                    form.type = reader.readString()
                    form.title = reader.readString()
                    form.description = reader.readString()
                    form.defaultValue = reader.readString()
                    form.step = reader.readFloat()
                    form.min = reader.readFloat()
                    form.max = reader.readFloat()
                    val labelsSize = reader.readVarInt()
                    for (l in 0 until labelsSize) {
                        form.labels[reader.readString()] = reader.readString()
                    }
                    btn.forms.add(form)
                }
                model.properties.extraAnimationButtons.add(btn)
            }

            val extraAnimationClassifyCount = reader.readVarInt()
            for (i in 0 until extraAnimationClassifyCount) {
                val classify = RawYsmModel.ExtraAnimationClassify()
                classify.id = reader.readString()
                val classificationExtrasCount = reader.readVarInt()
                for (j in 0 until classificationExtrasCount) {
                    classify.extras[reader.readString()] = reader.readString()
                }
                model.properties.extraAnimationClassifies.add(classify)
            }
        }

        model.properties.defaultTexture = reader.readString()
        model.properties.previewAnimation = reader.readString()
        model.properties.isFree = reader.readVarInt() != 0

        if (format > 4) {
            model.properties.renderLayersFirst = reader.readVarInt() != 0
        }

        if (format >= 15) {
            model.properties.allCutout = reader.readVarInt() != 0
            model.properties.disablePreviewRotation = reader.readVarInt() != 0
        }

        if (format > 15) {
            model.properties.guiNoLighting = reader.readVarInt() != 0
            if (format >= 32) {
                model.properties.mergeMultilineExpr = reader.readVarInt() != 0
                if (format >= 40) {
                    model.properties.isCustomSkinModel = reader.readVarInt() != 0
                    model.properties.useMcDefaultTexture = reader.readVarInt()
                }
            }

            model.properties.guiForeground = reader.readString()
            model.properties.guiBackground = reader.readString()

            val avatarsCount = reader.readVarInt()
            for (i in 0 until avatarsCount) {
                val avatar = RawYsmModel.RawImage()
                avatar.name = reader.readString()
                avatar.data = reader.readByteArray()
                avatar.width = reader.readVarInt()
                avatar.height = reader.readVarInt()
                avatar.format = reader.readVarInt()
                avatar.unknownFlag = reader.readVarInt()
                if (i < model.metadata.authors.size) {
                    model.metadata.authors[i].avatar = avatar.name ?: ""
                    model.metadata.authors[i].avatarImage = avatar
                } else {
                    model.metadata.extraAvatars.add(avatar)
                }
            }
        }

        if (format <= 15) return

        val backgroundImagesCount = reader.readVarInt()
        for (i in 0 until backgroundImagesCount) {
            val bg = RawYsmModel.RawImage()
            bg.name = reader.readString()
            bg.data = reader.readByteArray()
            bg.width = reader.readVarInt()
            bg.height = reader.readVarInt()
            bg.format = reader.readVarInt()
            bg.unknownFlag = reader.readVarInt()
            model.properties.backgroundImages.add(bg)
        }
    }

    private fun parseLegacyYSMInfo() {
        model.metadata.name = reader.readString()
        model.metadata.tips = reader.readString()
        val extraAnimationsCount = reader.readVarInt()
        for (i in 0 until extraAnimationsCount) {
            reader.readString()
        }
        val authorsCount = reader.readVarInt()
        for (i in 0 until authorsCount) {
            val author = RawYsmModel.RawMetadata.Author()
            author.name = reader.readString()
            model.metadata.authors.add(author)
        }
        model.metadata.licenseType = reader.readString()
        model.properties.isFree = reader.readVarInt() != 0
    }

    private fun parseAnimations(): RawYsmModel.RawAnimationFile {
        val animFile = RawYsmModel.RawAnimationFile()

        val animationCount = reader.readVarInt()
        for (animIndex in 0 until animationCount) {
            val anim = RawYsmModel.RawAnimation()
            anim.name = reader.readString()
            anim.length = reader.readFloat() / 20.0f
            anim.loopMode = reader.readVarInt()

            if (format > 9) {
                anim.unkInt1 = reader.readVarInt()
                anim.unkInt2 = reader.readVarInt()
                val blendWeightMolangCount = reader.readVarInt()
                for (i in 0 until blendWeightMolangCount) {
                    val datatype = reader.readByte().toInt()
                    when (datatype) {
                        0x01 -> {
                            anim.blendWeight = reader.readFloat()
                        }

                        0x02 -> {
                            anim.blendWeight = reader.readString()
                        }
                    }
                }
                anim.unkInt4 = reader.readVarInt()
            }

            val boneCount = reader.readVarInt()
            for (i in 0 until boneCount) {
                val ba = RawYsmModel.RawBoneAnimation()
                ba.boneName = reader.readString()
                parseChannel(ba.rotation)
                parseChannel(ba.position)
                parseChannel(ba.scale)
                anim.boneAnimations.add(ba)
            }

            // Timeline
            val timelineEventGroupsCount = reader.readVarInt()
            for (i in 0 until timelineEventGroupsCount) {
                val event = RawYsmModel.RawTimelineEvent()
                val timelineEventsCount = reader.readVarInt()
                for (j in 0 until timelineEventsCount) {
                    event.events.add(reader.readString())
                }
                event.timestamp = reader.readFloat() / 20.0f
                anim.timelineEvents.add(event)
            }

            // Effects
            if (format > 9) {
                val soundEffectsCount = reader.readVarInt()
                for (i in 0 until soundEffectsCount) {
                    val sfx = RawYsmModel.RawSoundEffect()
                    sfx.effectName = reader.readString()
                    sfx.timestamp = reader.readFloat() / 20.0f
                    anim.soundEffects.add(sfx)
                }
            }
            animFile.animations[anim.name ?: ""] = anim
        }

        return animFile
    }

    private fun parseChannel(channel: MutableList<RawYsmModel.RawKeyframe>) {
        val keyframeCount = reader.readVarInt()
        if (keyframeCount == 0) return

        for (i in 0 until keyframeCount) {
            val kf = RawYsmModel.RawKeyframe()
            kf.timestamp = reader.readFloat() / 20.0f
            kf.interpolationMode = reader.readVarInt()

            val firstData = arrayOfNulls<Any>(3)
            for (j in 0 until 3) {
                val datatype = reader.readByte().toInt()
                when (datatype) {
                    0x01 -> {
                        firstData[j] = reader.readFloat()
                    }

                    0x02 -> {
                        firstData[j] = reader.readString()
                    }
                }
            }

            kf.hasPreData = reader.readVarInt() > 0
            if (kf.hasPreData) {
                for (j in 0 until 3) {
                    val datatype = reader.readByte().toInt()
                    when (datatype) {
                        0x01 -> {
                            kf.postData[j] = reader.readFloat()
                        }

                        0x02 -> {
                            kf.postData[j] = reader.readString()
                        }
                    }
                }
                kf.preData = firstData
                kf.hasPreData = true
            } else {
                kf.postData = firstData
                kf.hasPreData = false
            }
            channel.add(kf)
        }
    }

    private fun parseAnimationControllers(
        targetList: MutableList<RawYsmModel.RawAnimationControllerFile>,
        readName: Boolean
    ) {
        val controllerCount = reader.readVarInt()
        for (i in 0 until controllerCount) {
            val file = RawYsmModel.RawAnimationControllerFile()

            if (format <= 15) {
                file.legacyUnknownInt = reader.readVarInt()
                file.name = "legacy_controller_$i"
            } else {
                if (readName) file.name = reader.readString()
                file.hash = reader.readString()
            }

            parseAnimationControllerBody(file.controllers)
            targetList.add(file)
        }
    }

    private fun parseAnimationControllerBody(targetMap: MutableMap<String, RawYsmModel.RawAnimationController>) {
        val animationCount = reader.readVarInt()
        for (animIndex in 0 until animationCount) {
            val entry = RawYsmModel.RawAnimationController()
            entry.animationName = reader.readString()
            entry.initialState = reader.readString()

            val statesCount = reader.readVarInt()
            for (s in 0 until statesCount) {
                val state = RawYsmModel.RawControllerState()
                state.name = reader.readString()

                val animationsSize = reader.readVarInt()
                for (j in 0 until animationsSize) {
                    state.animations[reader.readString()] = reader.readString()
                }
                val transitionsSize = reader.readVarInt()
                for (j in 0 until transitionsSize) {
                    state.transitions[reader.readString()] = reader.readString()
                }
                val onEntryCount = reader.readVarInt()
                for (j in 0 until onEntryCount) {
                    state.onEntry.add(reader.readString())
                }
                val onExitCount = reader.readVarInt()
                for (j in 0 until onExitCount) {
                    state.onExit.add(reader.readString())
                }
                if (reader.readVarInt() != 0) {
                    state.blendTransitionValue = reader.readFloat()
                } else {
                    val blendTransitionsCount = reader.readVarInt()
                    for (j in 0 until blendTransitionsCount) {
                        state.blendTransitions[reader.readFloat()] = reader.readFloat()
                    }
                }
                state.blendViaShortestPath = reader.readVarInt() != 0
                if (format > 26) {
                    val soundEffectsCount = reader.readVarInt()
                    for (j in 0 until soundEffectsCount) {
                        state.soundEffects.add(reader.readString())
                    }
                }
                entry.states.add(state)
            }
            targetMap[entry.animationName ?: ""] = entry
        }
    }

    private fun parseSoundFiles() {
        val soundCount = reader.readVarInt()
        for (i in 0 until soundCount) {
            val soundName = reader.readString()
            var hash = ""
            if (format > 15) {
                hash = reader.readString()
            }
            val data = reader.readByteArray()
            model.soundFiles[soundName] = RawYsmModel.RawDataFile(hash, data)
        }
    }

    private fun parseFunctionFiles() {
        val functionCount = reader.readVarInt()
        for (i in 0 until functionCount) {
            val functionName = reader.readString()
            val hash = reader.readString()
            val data = reader.readByteArray()
            model.functionFiles[functionName] = RawYsmModel.RawDataFile(hash, data)
        }
    }

    private fun parseLanguageFiles() {
        val languageCount = reader.readVarInt()
        for (i in 0 until languageCount) {
            val languageName = reader.readString()
            val hash = reader.readString()
            val nodesCount = reader.readVarInt()
            val langMap = LinkedHashMap<String, String>()
            for (j in 0 until nodesCount) {
                langMap[reader.readString()] = reader.readString()
            }
            val rawLangFile = RawYsmModel.RawLanguageFile(hash, langMap)
            model.languageFiles[languageName] = rawLangFile
            val normalized = languageName.lowercase(java.util.Locale.ROOT).replace('-', '_')
            if (normalized != languageName) {
                model.languageFiles[normalized] = rawLangFile
            }
        }
    }

    private fun parseTextureFiles(targetMap: MutableMap<String, RawYsmModel.RawTexture>) {
        val textureCount = reader.readVarInt()
        for (i in 0 until textureCount) {
            val tex = RawYsmModel.RawTexture()
            tex.name = reader.readString()
            tex.hash = reader.readString()
            tex.data = reader.readByteArray()
            tex.width = reader.readVarInt()
            tex.height = reader.readVarInt()
            tex.imageFormat = reader.readVarInt()
            tex.unknownFlag = reader.readVarInt()

            val subTextureSize = reader.readVarInt()
            for (j in 0 until subTextureSize) {
                val subTex = RawYsmModel.RawTexture.SubTexture()
                subTex.specularType = reader.readVarInt()
                val specRes = parseSpecialImage()
                subTex.hash = specRes.hash
                subTex.data = specRes.data
                subTex.width = reader.readVarInt()
                subTex.height = reader.readVarInt()
                subTex.imageFormat = reader.readVarInt()
                subTex.unknownFlag = reader.readVarInt()
                tex.subTextures.add(subTex)
            }
            targetMap[tex.name ?: ""] = tex
        }
    }

    private fun parseSpecialImage(): SpecialImageResult {
        val imageHash = reader.readString()
        val imageData = reader.readByteArray()
        return SpecialImageResult(imageHash, imageData)
    }

    private fun readVector3D(): FloatArray {
        return floatArrayOf(reader.readFloat(), reader.readFloat(), reader.readFloat())
    }

    private fun assignMainModels(tempMainModels: List<RawYsmModel.RawGeometry>) {
        for (tempMainModel in tempMainModels) {
            when (tempMainModel.modelType) {
                1 -> model.mainEntity.mainModel = tempMainModel
                2 -> model.mainEntity.armModel = tempMainModel
                3 -> {
                    val subEntity = RawYsmModel.RawSubEntity()
                    subEntity.model = tempMainModel
                    subEntity.identifier = "minecraft:arrow"
                    model.projectiles[subEntity.identifier ?: ""] = subEntity
                }

                else -> throw RuntimeException("Unknown model type: ${tempMainModel.modelType}")
            }
        }
    }

    override fun close() {
        reader.close()
    }

    private data class SpecialImageResult(val hash: String, val data: ByteArray) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SpecialImageResult

            if (hash != other.hash) return false
            if (!data.contentEquals(other.data)) return false
            return true
        }

        override fun hashCode(): Int {
            var result = hash.hashCode()
            result = 31 * result + data.contentHashCode()
            return result
        }
    }
}
