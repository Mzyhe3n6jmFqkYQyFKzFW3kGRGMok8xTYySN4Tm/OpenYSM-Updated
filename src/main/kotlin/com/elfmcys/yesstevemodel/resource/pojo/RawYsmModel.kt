package com.elfmcys.yesstevemodel.resource.pojo

class RawYsmModel {
    @JvmField
    var modelId: String? = null

    @JvmField
    var formatVersion: Int = 0

    @JvmField
    var metadata: RawMetadata = RawMetadata()

    @JvmField
    var properties: RawProperties = RawProperties()

    @JvmField
    var mainEntity: RawMainEntity = RawMainEntity()

    @JvmField
    var vehicles: MutableMap<String, RawSubEntity> = LinkedHashMap()

    @JvmField
    var projectiles: MutableMap<String, RawSubEntity> = LinkedHashMap()

    @JvmField
    var soundFiles: MutableMap<String, RawDataFile> = LinkedHashMap()

    @JvmField
    var functionFiles: MutableMap<String, RawDataFile> = LinkedHashMap()

    @JvmField
    var languageFiles: MutableMap<String, RawLanguageFile> = LinkedHashMap()

    @JvmField
    var footer: RawFooter = RawFooter()

    class RawMainEntity {
        @JvmField
        var mainModel: RawGeometry? = null

        @JvmField
        var armModel: RawGeometry? = null

        @JvmField
        var textures: MutableMap<String, RawTexture> = LinkedHashMap()

        @JvmField
        var animationFiles: MutableMap<String, RawAnimationFile> = LinkedHashMap()

        @JvmField
        var animationControllerFiles: MutableList<RawAnimationControllerFile> = ArrayList()
    }

    class RawAnimationControllerFile {
        @JvmField
        var name: String? = null

        @JvmField
        var hash: String? = null

        @JvmField
        var legacyUnknownInt: Int = 0

        @JvmField
        var controllers: MutableMap<String, RawAnimationController> = LinkedHashMap()
    }

    class RawSubEntity {
        @JvmField
        var identifier: String? = null

        @JvmField
        var matchIds: Array<String>? = null

        @JvmField
        var model: RawGeometry? = null

        @JvmField
        var textures: MutableMap<String, RawTexture> = LinkedHashMap()

        @JvmField
        var animationFiles: MutableMap<String, RawAnimationFile> = LinkedHashMap()

        @JvmField
        var animationControllerFiles: MutableList<RawAnimationControllerFile> = ArrayList()
    }

    class RawGeometry {
        @JvmField
        var modelType: Int = 0 // 1=main, 2=arm, 3=arrow

        @JvmField
        var identifier: String = ""

        @JvmField
        var sha256: String = ""

        @JvmField
        var textureWidth: Float = 64f

        @JvmField
        var textureHeight: Float = 64f

        @JvmField
        var visibleBoundsWidth: Float = 0.0f

        @JvmField
        var visibleBoundsHeight: Float = 0.0f

        @JvmField
        var visibleBoundsOffset: FloatArray? = null

        @JvmField
        var unkFloat1: Float = 0.0f

        @JvmField
        var unkFloat2: Float = 0.0f

        @JvmField
        var footerPad1: Int = 0

        @JvmField
        var footerPad2: Int = 0

        @JvmField
        var footerPad3: Int = 0

        @JvmField
        var bones: MutableList<RawBone> = ArrayList()
    }

    class RawBone {
        @JvmField
        var name: String? = null

        @JvmField
        var parentName: String? = null

        @JvmField
        var pivot: FloatArray = FloatArray(3)

        @JvmField
        var rotation: FloatArray = FloatArray(3)

        @JvmField
        var unkPad1: Int = 0

        @JvmField
        var unkPad2: Int = 0

        @JvmField
        var unkPad3: Int = 0

        @JvmField
        var unkPad4: Int = 0

        @JvmField
        var unkPad5: Int = 0

        @JvmField
        var cubes: MutableList<RawCube> = ArrayList()
    }

    class RawCube {
        @JvmField
        var faces: MutableList<RawFace> = ArrayList()

        @JvmField
        var unkInt1: Int = 0

        @JvmField
        var unkInt2: Int = 0

        @JvmField
        var unkInt3: Int = 0
    }

    class RawFace {
        @JvmField
        var normal: FloatArray = FloatArray(3)

        @JvmField
        var positions: Array<FloatArray> = Array(4) { FloatArray(3) }

        @JvmField
        var u: FloatArray = FloatArray(4)

        @JvmField
        var v: FloatArray = FloatArray(4)
    }

    class RawAnimationFile {
        @JvmField
        var animType: Int = 0

        @JvmField
        var fileHash: String? = null

        @JvmField
        var animations: MutableMap<String, RawAnimation> = LinkedHashMap()
    }

    class RawAnimation {
        @JvmField
        var name: String? = null

        @JvmField
        var length: Float = 0.0f

        @JvmField
        var loopMode: Int = 0

        @JvmField
        var blendWeight: Any? = null

        @JvmField
        var unkInt1: Int = 0

        @JvmField
        var unkInt2: Int = 0

        @JvmField
        var unkInt4: Int = 0

        @JvmField
        var boneAnimations: MutableList<RawBoneAnimation> = ArrayList()

        @JvmField
        var timelineEvents: MutableList<RawTimelineEvent> = ArrayList()

        @JvmField
        var soundEffects: MutableList<RawSoundEffect> = ArrayList()
    }

    class RawBoneAnimation {
        @JvmField
        var boneName: String? = null

        @JvmField
        var rotation: MutableList<RawKeyframe> = ArrayList()

        @JvmField
        var position: MutableList<RawKeyframe> = ArrayList()

        @JvmField
        var scale: MutableList<RawKeyframe> = ArrayList()
    }

    class RawKeyframe {
        @JvmField
        var timestamp: Float = 0.0f

        @JvmField
        var interpolationMode: Int = 0

        @JvmField
        var postData: Array<Any?> = arrayOfNulls(3)

        @JvmField
        var preData: Array<Any?> = arrayOfNulls(3)

        @JvmField
        var hasPreData: Boolean = false
    }

    class RawTimelineEvent {
        @JvmField
        var timestamp: Float = 0.0f

        @JvmField
        var events: MutableList<String> = ArrayList()
    }

    class RawSoundEffect {
        @JvmField
        var effectName: String? = null

        @JvmField
        var timestamp: Float = 0.0f
    }

    class RawTexture {
        @JvmField
        var name: String? = null

        @JvmField
        var hash: String? = null

        @JvmField
        var width: Int = 0

        @JvmField
        var height: Int = 0

        @JvmField
        var imageFormat: Int = 0

        @JvmField
        var data: ByteArray? = null

        @JvmField
        var unknownFlag: Int = 0

        @JvmField
        var subTextures: MutableList<SubTexture> = ArrayList()

        class SubTexture {
            @JvmField
            var hash: String? = null

            @JvmField
            var specularType: Int = 0

            @JvmField
            var width: Int = 0

            @JvmField
            var height: Int = 0

            @JvmField
            var imageFormat: Int = 0

            @JvmField
            var data: ByteArray? = null

            @JvmField
            var unknownFlag: Int = 0
        }
    }

    class RawAnimationController {
        @JvmField
        var animationName: String? = null

        @JvmField
        var initialState: String? = null

        @JvmField
        var states: MutableList<RawControllerState> = ArrayList()
    }

    class RawControllerState {
        @JvmField
        var name: String? = null

        @JvmField
        var animations: MutableMap<String, String> = LinkedHashMap()

        @JvmField
        var transitions: MutableMap<String, String> = LinkedHashMap()

        @JvmField
        var onEntry: MutableList<String> = ArrayList()

        @JvmField
        var onExit: MutableList<String> = ArrayList()

        @JvmField
        var soundEffects: MutableList<String> = ArrayList()

        @JvmField
        var blendTransitionValue: Float = 0.0f

        @JvmField
        var blendViaShortestPath: Boolean = false

        @JvmField
        var blendTransitions: MutableMap<Float, Float> = LinkedHashMap()
    }

    class RawMetadata {
        @JvmField
        var name: String = ""

        @JvmField
        var tips: String = ""

        @JvmField
        var licenseType: String = ""

        @JvmField
        var licenseDescription: String = ""

        @JvmField
        var authors: MutableList<Author> = ArrayList()

        @JvmField
        var links: MutableMap<String, String> = LinkedHashMap()

        @JvmField
        var extraAvatars: MutableList<RawImage> = ArrayList()

        class Author {
            @JvmField
            var name: String = ""

            @JvmField
            var role: String = ""

            @JvmField
            var comment: String = ""

            @JvmField
            var contacts: MutableMap<String, String> = LinkedHashMap()

            @JvmField
            var avatar: String = ""

            @JvmField
            var avatarImage: RawImage? = null
        }
    }

    class RawImage {
        @JvmField
        var name: String? = null

        @JvmField
        var data: ByteArray? = null

        @JvmField
        var width: Int = 0

        @JvmField
        var height: Int = 0

        @JvmField
        var format: Int = 0

        @JvmField
        var unknownFlag: Int = 0
    }

    class RawProperties {
        @JvmField
        var sha256: String = ""

        @JvmField
        var widthScale: Float = 0.7f

        @JvmField
        var heightScale: Float = 0.7f

        @JvmField
        var defaultTexture: String = "default"

        @JvmField
        var previewAnimation: String = ""

        @JvmField
        var isFree: Boolean = false

        @JvmField
        var renderLayersFirst: Boolean = false

        @JvmField
        var allCutout: Boolean = false

        @JvmField
        var disablePreviewRotation: Boolean = false

        @JvmField
        var isCustomSkinModel: Boolean = false

        @JvmField
        var guiNoLighting: Boolean = false

        @JvmField
        var mergeMultilineExpr: Boolean = false

        @JvmField
        var guiForeground: String = ""

        @JvmField
        var guiBackground: String = ""

        @JvmField
        var backgroundImages: MutableList<RawImage> = ArrayList()

        @JvmField
        var extraAnimations: MutableMap<String, String> = LinkedHashMap()

        @JvmField
        var extraAnimationClassifies: MutableList<ExtraAnimationClassify> = ArrayList()

        @JvmField
        var extraAnimationButtons: MutableList<ExtraAnimationButton> = ArrayList()
    }

    class ExtraAnimationClassify {
        @JvmField
        var id: String? = null

        @JvmField
        var extras: MutableMap<String, String> = LinkedHashMap()
    }

    class ExtraAnimationButton {
        @JvmField
        var id: String? = null

        @JvmField
        var name: String? = null

        @JvmField
        var description: String? = null

        @JvmField
        var forms: MutableList<ConfigForm> = ArrayList()
    }

    class ConfigForm {
        @JvmField
        var type: String? = null

        @JvmField
        var title: String? = null

        @JvmField
        var description: String? = null

        @JvmField
        var defaultValue: String? = null

        @JvmField
        var step: Float = 0.0f

        @JvmField
        var min: Float = 0.0f

        @JvmField
        var max: Float = 0.0f

        @JvmField
        var labels: MutableMap<String, String> = LinkedHashMap()
    }

    data class RawDataFile(@JvmField val hash: String, @JvmField val data: ByteArray?) {
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

    data class RawLanguageFile(@JvmField val hash: String, @JvmField val data: MutableMap<String, String>)

    class RawFooter {
        @JvmField
        var version: Int = 65535

        @JvmField
        var unkInt1: Int = 1

        @JvmField
        var rand: String = ""

        @JvmField
        var time: Long = 0L

        @JvmField
        var extra: String = ""

        @JvmField
        var unkInt2: Int = 0
    }
}
