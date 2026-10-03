package com.elfmcys.yesstevemodel.resource

import com.elfmcys.yesstevemodel.resource.pojo.RawYsmModel
import io.netty.buffer.Unpooled
import rip.ysm.security.YSMByteBuf
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.imageio.ImageIO

object YSMBinarySerializer {

    @JvmStatic
    fun serialize(model: RawYsmModel, format: Int, writeFooter: Boolean): YSMByteBuf {
        val buf = YSMByteBuf(Unpooled.buffer())
        if (format >= 16) {
            writeModern(buf, model, format)
            if (writeFooter) {
                writeFooter(buf, model)
            }
        } else {
            throw UnsupportedOperationException()
        }
        return buf
    }

    @JvmStatic
    fun writeFooter(buf: YSMByteBuf, model: RawYsmModel) {
        if (model.footer.version == 65535) {
            buf.writeVarInt(65535)
            buf.writeVarInt(0)
            buf.writeVarLong(0L)
            return
        }
        buf.writeVarInt(if (model.footer.version != 0) model.footer.version else 65535)
        buf.writeVarInt(model.footer.unkInt1)
        if (model.footer.unkInt1 != 0) {
            buf.writeString(model.footer.rand ?: "")
        }
        buf.writeVarLong(model.footer.time)
        if (model.footer.unkInt1 != 0) {
            buf.writeString(model.footer.extra ?: "")
            buf.writeVarInt(model.footer.unkInt2)
        }
    }

    @JvmStatic
    fun writeModern(buf: YSMByteBuf, model: RawYsmModel, format: Int) {
        writeSoundFiles(buf, model.soundFiles, format)
        writeFunctionFiles(buf, model.functionFiles)
        writeLanguageFiles(buf, model.languageFiles)
        if (format < 26) {
            throw UnsupportedOperationException()
        } else {
            writeSubEntities(buf, model.vehicles, format, "Vehicle")
            writeSubEntities(buf, model.projectiles, format, "Projectile")
        }
        buf.writeVarInt(1)

        val mainAnimFiles = model.mainEntity.animationFiles
        buf.writeVarInt(mainAnimFiles.size)
        for (animFile in mainAnimFiles.values) {
            buf.writeVarInt(animFile.animType)
            buf.writeString(animFile.fileHash ?: "")
            writeAnimationFileContent(buf, animFile, format)
        }

        writeAnimationControllers(buf, model.mainEntity.animationControllerFiles, format, true)
        writeTextureFiles(buf, model.mainEntity.textures)

        val geoList = ArrayList<RawYsmModel.RawGeometry>()
        if (model.mainEntity.mainModel != null) {
            geoList.add(model.mainEntity.mainModel!!)
        }
        if (model.mainEntity.armModel != null) {
            geoList.add(model.mainEntity.armModel!!)
        }
        buf.writeVarInt(geoList.size)
        for (geo in geoList) {
            buf.writeVarInt(geo.modelType)
            buf.writeString(geo.sha256 ?: "")
            writeGeometry(buf, geo, format)
        }
        writeYsmJson(buf, model, format)
    }

    @JvmStatic
    fun writeSubEntities(
        buf: YSMByteBuf,
        entities: Map<String, RawYsmModel.RawSubEntity>,
        format: Int,
        category: String
    ) {
        val valid = ArrayList<RawYsmModel.RawSubEntity>()
        for (sub in entities.values) {
            if (sub.model != null && sub.textures.isNotEmpty()) {
                valid.add(sub)
            }
        }
        buf.writeVarInt(valid.size)
        var index = 0
        for (sub in valid) {
            buf.writeVarInt(sub.animationFiles.size)
            for (animFile in sub.animationFiles.values) {
                buf.writeString(animFile.fileHash ?: "")
                writeAnimationFileContent(buf, animFile, format)
            }
            writeAnimationControllers(buf, sub.animationControllerFiles, format, false)

            val baseTex = sub.textures.values.iterator().next()
            var baseData = baseTex.data
            var baseFormat = baseTex.imageFormat
            if (baseFormat == -1) {
                baseData = convertRgbaToPng(baseData, baseTex.width, baseTex.height)
                baseFormat = 2
            }
            writeSpecialImage(buf, baseTex.hash, baseData)
            buf.writeVarInt(baseTex.width)
            buf.writeVarInt(baseTex.height)
            buf.writeVarInt(baseFormat)
            buf.writeVarInt(baseTex.unknownFlag)
            buf.writeVarInt(baseTex.subTextures.size)
            for (subTex in baseTex.subTextures) {
                buf.writeVarInt(subTex.specularType)
                var subData = subTex.data
                var subFormat = subTex.imageFormat
                if (subFormat == -1) {
                    subData = convertRgbaToPng(subData, subTex.width, subTex.height)
                    subFormat = 2
                }
                writeSpecialImage(buf, subTex.hash, subData)
                buf.writeVarInt(subTex.width)
                buf.writeVarInt(subTex.height)
                buf.writeVarInt(subFormat)
                buf.writeVarInt(subTex.unknownFlag)
            }
            buf.writeString(sub.model?.sha256 ?: "")
            writeGeometry(buf, sub.model!!, format)
            if (format > 26) {
                buf.writeVarInt(0x01)
                buf.writeString(sub.identifier ?: "")
            }
            index++
        }
    }

    @JvmStatic
    fun writeGeometry(buf: YSMByteBuf, geo: RawYsmModel.RawGeometry, format: Int) {
        buf.writeVarInt(geo.bones.size)
        for (bone in geo.bones) {
            buf.writeString(bone.parentName ?: "")
            buf.writeVarInt(bone.cubes.size)
            for (cube in bone.cubes) {
                buf.writeVarInt(cube.faces.size)
                for (face in cube.faces) {
                    writeVector3D(buf, face.normal)
                    for (v in 0 until 4) {
                        writeVector3D(buf, face.positions[v])
                        buf.writeFloat(face.u[v])
                        buf.writeFloat(face.v[v])
                    }
                }
                buf.writeVarInt(cube.unkInt1)
                buf.writeVarInt(cube.unkInt2)
                buf.writeVarInt(cube.unkInt3)
            }
            buf.writeString(bone.name ?: "")
            buf.writeVarInt(bone.unkPad1)
            buf.writeVarInt(bone.unkPad2)
            buf.writeVarInt(bone.unkPad3)
            buf.writeVarInt(bone.unkPad4)
            buf.writeVarInt(bone.unkPad5)
            writeVector3D(buf, bone.pivot)
            writeVector3D(buf, bone.rotation)
        }
        buf.writeString(geo.identifier ?: "")
        buf.writeFloat(geo.textureHeight)
        buf.writeFloat(geo.textureWidth)
        buf.writeFloat(geo.visibleBoundsHeight)
        buf.writeFloat(geo.visibleBoundsWidth)
        buf.writeVarInt(geo.visibleBoundsOffset?.size ?: 0)
        if (geo.visibleBoundsOffset != null) {
            for (v in geo.visibleBoundsOffset!!) {
                buf.writeFloat(v)
            }
        }
        buf.writeFloat(geo.unkFloat1)
        buf.writeFloat(geo.unkFloat2)
        buf.writeVarInt(0)
        buf.writeVarInt(geo.footerPad1)
        buf.writeVarInt(geo.footerPad2)
        buf.writeVarInt(geo.footerPad3)
    }

    @JvmStatic
    fun writeAnimationFileContent(buf: YSMByteBuf, animFile: RawYsmModel.RawAnimationFile, format: Int) {
        buf.writeVarInt(animFile.animations.size)
        for (anim in animFile.animations.values) {
            buf.writeString(anim.name ?: "")
            buf.writeFloat(anim.length * 20f)
            buf.writeVarInt(anim.loopMode)
            if (format > 9) {
                buf.writeVarInt(anim.unkInt1)
                buf.writeVarInt(anim.unkInt2)
                val hasBlend = if (anim.blendWeight != null) 1 else 0
                buf.writeVarInt(hasBlend)
                if (hasBlend > 0) {
                    writeMolangValue(buf, anim.blendWeight)
                }
                buf.writeVarInt(anim.unkInt4)
            }
            buf.writeVarInt(anim.boneAnimations.size)
            for (ba in anim.boneAnimations) {
                buf.writeString(ba.boneName ?: "")
                writeChannel(buf, ba.rotation)
                writeChannel(buf, ba.position)
                writeChannel(buf, ba.scale)
            }
            buf.writeVarInt(anim.timelineEvents.size)
            for (event in anim.timelineEvents) {
                buf.writeVarInt(event.events.size)
                for (e in event.events) {
                    buf.writeString(e)
                }
                buf.writeFloat(event.timestamp * 20f)
            }
            if (format > 9) {
                buf.writeVarInt(anim.soundEffects.size)
                for (sfx in anim.soundEffects) {
                    buf.writeString(sfx.effectName ?: "")
                    buf.writeFloat(sfx.timestamp * 20f)
                }
            }
        }
    }

    @JvmStatic
    fun writeChannel(buf: YSMByteBuf, keyframes: List<RawYsmModel.RawKeyframe>) {
        buf.writeVarInt(keyframes.size)
        for (kf in keyframes) {
            buf.writeFloat(kf.timestamp * 20f)
            buf.writeVarInt(kf.interpolationMode)
            if (kf.hasPreData) {
                for (i in 0 until 3) writeMolangValue(buf, kf.preData[i])
                buf.writeVarInt(1)
                for (i in 0 until 3) writeMolangValue(buf, kf.postData[i])
            } else {
                for (i in 0 until 3) writeMolangValue(buf, kf.postData[i])
                buf.writeVarInt(0)
            }
        }
    }

    @JvmStatic
    fun writeMolangValue(buf: YSMByteBuf, value: Any?) {
        when (value) {
            is Float -> {
                buf.writeByte(0x01)
                buf.writeFloat(value)
            }

            is Number -> {
                buf.writeByte(0x01)
                buf.writeFloat(value.toFloat())
            }

            is String -> {
                buf.writeByte(0x02)
                buf.writeString(value)
            }

            null -> {
                buf.writeByte(0x01)
                buf.writeFloat(0f)
            }

            else -> throw IllegalArgumentException("Unknown molang value type: ${value.javaClass}")
        }
    }

    @JvmStatic
    fun writeAnimationControllers(
        buf: YSMByteBuf,
        files: List<RawYsmModel.RawAnimationControllerFile>,
        format: Int,
        writeName: Boolean
    ) {
        buf.writeVarInt(files.size)
        for (file in files) {
            if (writeName) buf.writeString(file.name ?: "")
            buf.writeString(file.hash ?: "")
            writeAnimationControllerBody(buf, file.controllers, format)
        }
    }

    @JvmStatic
    fun writeAnimationControllerBody(
        buf: YSMByteBuf,
        controllers: Map<String, RawYsmModel.RawAnimationController>,
        format: Int
    ) {
        buf.writeVarInt(controllers.size)
        for (ac in controllers.values) {
            buf.writeString(ac.animationName ?: "")
            buf.writeString(ac.initialState ?: "")

            buf.writeVarInt(ac.states.size)
            for (state in ac.states) {
                buf.writeString(state.name ?: "")

                buf.writeVarInt(state.animations.size)
                for ((k, v) in state.animations) {
                    buf.writeString(k)
                    buf.writeString(v)
                }

                buf.writeVarInt(state.transitions.size)
                for ((k, v) in state.transitions) {
                    buf.writeString(k)
                    buf.writeString(v)
                }

                buf.writeVarInt(state.onEntry.size)
                for (s in state.onEntry) buf.writeString(s)

                buf.writeVarInt(state.onExit.size)
                for (s in state.onExit) buf.writeString(s)

                if (state.blendTransitions.isNotEmpty()) {
                    buf.writeVarInt(0)
                    buf.writeVarInt(state.blendTransitions.size)
                    for ((k, v) in state.blendTransitions) {
                        buf.writeFloat(k)
                        buf.writeFloat(v)
                    }
                } else {
                    buf.writeVarInt(1)
                    buf.writeFloat(state.blendTransitionValue)
                }

                buf.writeVarInt(if (state.blendViaShortestPath) 1 else 0)

                if (format > 26) {
                    buf.writeVarInt(state.soundEffects.size)
                    for (eff in state.soundEffects) buf.writeString(eff)
                }
            }
        }
    }

    @JvmStatic
    fun writeTextureFiles(buf: YSMByteBuf, textures: Map<String, RawYsmModel.RawTexture>) {
        buf.writeVarInt(textures.size)
        for (tex in textures.values) {
            buf.writeString(tex.name ?: "")
            buf.writeString(tex.hash ?: "")
            var texData = tex.data
            var texFormat = tex.imageFormat
            if (texFormat == -1) {
                texData = convertRgbaToPng(texData, tex.width, tex.height)
                texFormat = 2
            }
            buf.writeByteArray(texData ?: ByteArray(0))
            buf.writeVarInt(tex.width)
            buf.writeVarInt(tex.height)
            buf.writeVarInt(texFormat)
            buf.writeVarInt(tex.unknownFlag)

            buf.writeVarInt(tex.subTextures.size)
            for (sub in tex.subTextures) {
                buf.writeVarInt(sub.specularType)
                var subData = sub.data
                var subFormat = sub.imageFormat
                if (subFormat == -1) {
                    subData = convertRgbaToPng(subData, sub.width, sub.height)
                    subFormat = 2
                }
                writeSpecialImage(buf, sub.hash, subData)
                buf.writeVarInt(sub.width)
                buf.writeVarInt(sub.height)
                buf.writeVarInt(subFormat)
                buf.writeVarInt(sub.unknownFlag)
            }
        }
    }

    @JvmStatic
    fun writeSoundFiles(buf: YSMByteBuf, sounds: Map<String, RawYsmModel.RawDataFile>, format: Int) {
        buf.writeVarInt(sounds.size)
        for ((key, value) in sounds) {
            buf.writeString(key)
            if (format > 15) {
                buf.writeString(value.hash)
            }
            buf.writeByteArray(value.data ?: ByteArray(0))
        }
    }

    @JvmStatic
    fun writeFunctionFiles(buf: YSMByteBuf, functions: Map<String, RawYsmModel.RawDataFile>) {
        buf.writeVarInt(functions.size)
        for ((key, value) in functions) {
            buf.writeString(key)
            buf.writeString(value.hash)
            buf.writeByteArray(value.data ?: ByteArray(0))
        }
    }

    @JvmStatic
    fun writeLanguageFiles(buf: YSMByteBuf, languages: Map<String, RawYsmModel.RawLanguageFile>) {
        buf.writeVarInt(languages.size)
        for ((key, value) in languages) {
            buf.writeString(key)
            buf.writeString(value.hash)
            val nodes = value.data
            buf.writeVarInt(nodes.size)
            for ((k, v) in nodes) {
                buf.writeString(k)
                buf.writeString(v)
            }
        }
    }

    @JvmStatic
    fun writeSpecialImage(buf: YSMByteBuf, hash: String?, data: ByteArray?) {
        buf.writeString(hash ?: "")
        buf.writeByteArray(data ?: ByteArray(0))
    }

    @JvmStatic
    fun writeVector3D(buf: YSMByteBuf, vec: FloatArray) {
        buf.writeFloat(vec[0])
        buf.writeFloat(vec[1])
        buf.writeFloat(vec[2])
    }

    @JvmStatic
    fun writeYsmJson(buf: YSMByteBuf, model: RawYsmModel, format: Int) {
        val props = model.properties
        val meta = model.metadata

        buf.writeString(props.sha256)
        buf.writeVarInt(1) // isNewVersionYsm

        buf.writeString(meta.name)
        buf.writeString(meta.tips)
        buf.writeString(meta.licenseType)
        buf.writeString(meta.licenseDescription)

        buf.writeVarInt(meta.authors.size)
        for (author in meta.authors) {
            buf.writeString(author.name)
            buf.writeString(author.role)
            buf.writeVarInt(author.contacts.size)
            for ((k, v) in author.contacts) {
                buf.writeString(k)
                buf.writeString(v)
            }
            buf.writeString(author.comment)
        }

        buf.writeVarInt(meta.links.size)
        for ((k, v) in meta.links) {
            buf.writeString(k)
            buf.writeString(v)
        }

        buf.writeFloat(props.widthScale)
        buf.writeFloat(props.heightScale)

        buf.writeVarInt(props.extraAnimations.size)
        for ((k, v) in props.extraAnimations) {
            buf.writeString(k)
            buf.writeString(v)
        }

        if (format > 9) {
            buf.writeVarInt(props.extraAnimationButtons.size)
            for (btn in props.extraAnimationButtons) {
                buf.writeString(btn.id ?: "")
                buf.writeString(btn.name ?: "")
                buf.writeVarInt(0)

                buf.writeVarInt(btn.forms.size)
                for (form in btn.forms) {
                    buf.writeString(form.type ?: "")
                    buf.writeString(form.title ?: "")
                    buf.writeString(form.description ?: "")
                    buf.writeString(form.defaultValue ?: "")
                    buf.writeFloat(form.step)
                    buf.writeFloat(form.min)
                    buf.writeFloat(form.max)
                    buf.writeVarInt(form.labels.size)
                    for ((k, v) in form.labels) {
                        buf.writeString(k)
                        buf.writeString(v)
                    }
                }
            }

            buf.writeVarInt(props.extraAnimationClassifies.size)
            for (cls in props.extraAnimationClassifies) {
                buf.writeString(cls.id ?: "")
                buf.writeVarInt(cls.extras.size)
                for ((k, v) in cls.extras) {
                    buf.writeString(k)
                    buf.writeString(v)
                }
            }
        }

        buf.writeString(props.defaultTexture)
        buf.writeString(props.previewAnimation)
        buf.writeVarInt(if (props.isFree) 1 else 0)

        if (format > 4) {
            buf.writeVarInt(if (props.renderLayersFirst) 1 else 0)
        }
        if (format >= 15) {
            buf.writeVarInt(if (props.allCutout) 1 else 0)
            buf.writeVarInt(if (props.disablePreviewRotation) 1 else 0)
        }

        if (format > 15) {
            buf.writeVarInt(if (props.guiNoLighting) 1 else 0)
            if (format >= 32) {
                buf.writeVarInt(if (props.mergeMultilineExpr) 1 else 0)
            }
            buf.writeString(props.guiForeground)
            buf.writeString(props.guiBackground)

            val avatars = ArrayList<RawYsmModel.RawImage>()
            for (author in meta.authors) {
                if (author.avatarImage != null) {
                    avatars.add(author.avatarImage!!)
                }
            }
            avatars.addAll(meta.extraAvatars)
            buf.writeVarInt(avatars.size)
            for (img in avatars) {
                buf.writeString(img.name ?: "")
                var imgData = img.data
                var imgFormat = img.format
                if (imgFormat == -1) {
                    imgData = convertRgbaToPng(imgData, img.width, img.height)
                    imgFormat = 2
                }
                buf.writeByteArray(imgData ?: ByteArray(0))
                buf.writeVarInt(img.width)
                buf.writeVarInt(img.height)
                buf.writeVarInt(imgFormat)
                buf.writeVarInt(img.unknownFlag)
            }
        }

        if (format > 15) {
            buf.writeVarInt(props.backgroundImages.size)
            for (bg in props.backgroundImages) {
                buf.writeString(bg.name ?: "")
                var bgData = bg.data
                var bgFormat = bg.format
                if (bgFormat == -1) {
                    bgData = convertRgbaToPng(bgData, bg.width, bg.height)
                    bgFormat = 2
                }
                buf.writeByteArray(bgData ?: ByteArray(0))
                buf.writeVarInt(bg.width)
                buf.writeVarInt(bg.height)
                buf.writeVarInt(bgFormat)
                buf.writeVarInt(bg.unknownFlag)
            }
        }
    }

    @JvmStatic
    fun convertRgbaToPng(rgbaData: ByteArray?, width: Int, height: Int): ByteArray? {
        if (rgbaData == null || width <= 0 || height <= 0 || rgbaData.size < width * height * 4) {
            return rgbaData
        }

        val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val pixels = IntArray(width * height)

        for (i in pixels.indices) {
            val r = rgbaData[i * 4].toInt() and 0xFF
            val g = rgbaData[i * 4 + 1].toInt() and 0xFF
            val b = rgbaData[i * 4 + 2].toInt() and 0xFF
            val a = rgbaData[i * 4 + 3].toInt() and 0xFF
            pixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
        }

        img.setRGB(0, 0, width, height, pixels, 0, width)

        return try {
            ByteArrayOutputStream().use { baos ->
                ImageIO.write(img, "PNG", baos)
                baos.toByteArray()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            rgbaData
        }
    }
}
