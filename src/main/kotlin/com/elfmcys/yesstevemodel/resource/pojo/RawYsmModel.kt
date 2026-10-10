package com.elfmcys.yesstevemodel.resource.pojo

class RawYsmModel {
    var modelId: String? = null
    var formatVersion: Int = 0
    var metadata: RawMetadata = RawMetadata()
    var properties: RawProperties = RawProperties()
    var mainEntity: RawMainEntity = RawMainEntity()
    var vehicles: MutableMap<String, RawSubEntity> = LinkedHashMap()
    var projectiles: MutableMap<String, RawSubEntity> = LinkedHashMap()
    var soundFiles: MutableMap<String, RawDataFile> = LinkedHashMap()
    var functionFiles: MutableMap<String, RawDataFile> = LinkedHashMap()
    var languageFiles: MutableMap<String, RawLanguageFile> = LinkedHashMap()
    var footer: RawFooter = RawFooter()

    class RawMainEntity {
        var mainModel: RawGeometry? = null
        var armModel: RawGeometry? = null
        var textures: MutableMap<String, RawTexture> = LinkedHashMap()
        var animationFiles: MutableMap<String, RawAnimationFile> = LinkedHashMap()
        var animationControllerFiles: MutableList<RawAnimationControllerFile> = ArrayList()
    }

    class RawAnimationControllerFile {
        var name: String? = null
        var hash: String? = null
        var legacyUnknownInt: Int = 0
        var controllers: MutableMap<String, RawAnimationController> = LinkedHashMap()
    }

    class RawSubEntity {
        var identifier: String? = null
        var matchIds: Array<String>? = null
        var model: RawGeometry? = null
        var textures: MutableMap<String, RawTexture> = LinkedHashMap()
        var animationFiles: MutableMap<String, RawAnimationFile> = LinkedHashMap()
        var animationControllerFiles: MutableList<RawAnimationControllerFile> = ArrayList()
    }

    class RawGeometry {
        var modelType: Int = 0 // 1=main, 2=arm, 3=arrow
        var identifier: String = ""
        var sha256: String = ""
        var textureWidth: Float = 64f
        var textureHeight: Float = 64f
        var visibleBoundsWidth: Float = 0.0f
        var visibleBoundsHeight: Float = 0.0f
        var visibleBoundsOffset: FloatArray? = null
        var unkFloat1: Float = 0.0f
        var unkFloat2: Float = 0.0f
        var footerPad1: Int = 0
        var footerPad2: Int = 0
        var footerPad3: Int = 0
        var bones: MutableList<RawBone> = ArrayList()
    }

    class RawBone {
        var name: String? = null
        var parentName: String? = null
        var pivot: FloatArray = FloatArray(3)
        var rotation: FloatArray = FloatArray(3)
        var unkPad1: Int = 0
        var unkPad2: Int = 0
        var unkPad3: Int = 0
        var unkPad4: Int = 0
        var unkPad5: Int = 0
        var cubes: MutableList<RawCube> = ArrayList()
    }

    class RawCube {
        var faces: MutableList<RawFace> = ArrayList()
        var unkInt1: Int = 0
        var unkInt2: Int = 0
        var unkInt3: Int = 0
    }

    class RawFace {
        var normal: FloatArray = FloatArray(3)
        var positions: Array<FloatArray> = Array(4) { FloatArray(3) }
        var u: FloatArray = FloatArray(4)
        var v: FloatArray = FloatArray(4)
    }

    class RawAnimationFile {
        var animType: Int = 0
        var fileHash: String? = null
        var animations: MutableMap<String, RawAnimation> = LinkedHashMap()
    }

    class RawAnimation {
        var name: String? = null
        var length: Float = 0.0f
        var loopMode: Int = 0
        var blendWeight: Any? = null
        var unkInt1: Int = 0
        var unkInt2: Int = 0
        var unkInt4: Int = 0
        var boneAnimations: MutableList<RawBoneAnimation> = ArrayList()
        var timelineEvents: MutableList<RawTimelineEvent> = ArrayList()
        var soundEffects: MutableList<RawSoundEffect> = ArrayList()
    }

    class RawBoneAnimation {
        var boneName: String? = null
        var rotation: MutableList<RawKeyframe> = ArrayList()
        var position: MutableList<RawKeyframe> = ArrayList()
        var scale: MutableList<RawKeyframe> = ArrayList()
    }

    class RawKeyframe {
        var timestamp: Float = 0.0f
        var interpolationMode: Int = 0
        var postData: Array<Any?> = arrayOfNulls(3)
        var preData: Array<Any?> = arrayOfNulls(3)
        var hasPreData: Boolean = false
    }

    class RawTimelineEvent {
        var timestamp: Float = 0.0f
        var events: MutableList<String> = ArrayList()
    }

    class RawSoundEffect {
        var effectName: String? = null
        var timestamp: Float = 0.0f
    }

    class RawTexture {
        var name: String? = null
        var hash: String? = null
        var width: Int = 0
        var height: Int = 0
        var imageFormat: Int = 0
        var data: ByteArray? = null
        var unknownFlag: Int = 0
        var subTextures: MutableList<SubTexture> = ArrayList()

        class SubTexture {
            var hash: String? = null
            var specularType: Int = 0
            var width: Int = 0
            var height: Int = 0
            var imageFormat: Int = 0
            var data: ByteArray? = null
            var unknownFlag: Int = 0
        }
    }

    class RawAnimationController {
        var animationName: String? = null
        var initialState: String? = null
        var states: MutableList<RawControllerState> = ArrayList()
    }

    class RawControllerState {
        var name: String? = null
        var animations: MutableMap<String, String> = LinkedHashMap()
        var transitions: MutableMap<String, String> = LinkedHashMap()
        var onEntry: MutableList<String> = ArrayList()
        var onExit: MutableList<String> = ArrayList()
        var soundEffects: MutableList<String> = ArrayList()
        var blendTransitionValue: Float = 0.0f
        var blendViaShortestPath: Boolean = false
        var blendTransitions: MutableMap<Float, Float> = LinkedHashMap()
    }

    class RawMetadata {
        var name: String = ""
        var tips: String = ""
        var licenseType: String = ""
        var licenseDescription: String = ""
        var authors: MutableList<Author> = ArrayList()
        var links: MutableMap<String, String> = LinkedHashMap()
        var extraAvatars: MutableList<RawImage> = ArrayList()

        class Author {
            var name: String = ""
            var role: String = ""
            var comment: String = ""
            var contacts: MutableMap<String, String> = LinkedHashMap()
            var avatar: String = ""
            var avatarImage: RawImage? = null
        }
    }

    class RawImage {
        var name: String? = null
        var data: ByteArray? = null
        var width: Int = 0
        var height: Int = 0
        var format: Int = 0
        var unknownFlag: Int = 0
    }

    class RawProperties {
        var sha256: String = ""
        var widthScale: Float = 0.7f
        var heightScale: Float = 0.7f
        var defaultTexture: String = "default"
        var previewAnimation: String = ""
        var isFree: Boolean = false
        var renderLayersFirst: Boolean = false
        var allCutout: Boolean = false
        var disablePreviewRotation: Boolean = false
        var isCustomSkinModel: Boolean = false
        var guiNoLighting: Boolean = false
        var mergeMultilineExpr: Boolean = false
        var guiForeground: String = ""
        var guiBackground: String = ""
        var backgroundImages: MutableList<RawImage> = ArrayList()
        var extraAnimations: MutableMap<String, String> = LinkedHashMap()
        var extraAnimationClassifies: MutableList<ExtraAnimationClassify> = ArrayList()
        var extraAnimationButtons: MutableList<ExtraAnimationButton> = ArrayList()
    }

    class ExtraAnimationClassify {
        var id: String? = null
        var extras: MutableMap<String, String> = LinkedHashMap()
    }

    class ExtraAnimationButton {
        var id: String? = null
        var name: String? = null
        var description: String? = null
        var forms: MutableList<ConfigForm> = ArrayList()
    }

    class ConfigForm {
        var type: String? = null
        var title: String? = null
        var description: String? = null
        var defaultValue: String? = null
        var step: Float = 0.0f
        var min: Float = 0.0f
        var max: Float = 0.0f
        var labels: MutableMap<String, String> = LinkedHashMap()
    }

    data class RawDataFile(val hash: String, val data: ByteArray?) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as RawDataFile
            if (hash != other.hash) return false
            if (!data.contentEquals(other.data)) return false
            return true
        }

        override fun hashCode(): Int {
            var result = hash.hashCode()
            result = 31 * result + (data?.contentHashCode() ?: 0)
            return result
        }
    }

    data class RawLanguageFile(val hash: String, val data: MutableMap<String, String>)

    class RawFooter {
        var version: Int = 65535
        var unkInt1: Int = 1
        var rand: String = ""
        var time: Long = 0L
        var extra: String = ""
        var unkInt2: Int = 0
    }
}
