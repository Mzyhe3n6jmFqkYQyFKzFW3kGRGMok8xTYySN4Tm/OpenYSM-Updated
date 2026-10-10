package rip.ysm.gpu

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.NativeLibLoader
import com.mojang.blaze3d.systems.RenderSystem
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GLCapabilities
import java.util.*

object GpuCapability {
    @Volatile
    private var checked: Boolean = false

    @Volatile
    private var available: Boolean = false

    @Volatile
    private var _reason: String? = null

    val isAvailable: Boolean
        get() {
            if (!checked) check()
            return available
        }

    val reason: String?
        get() {
            if (!checked) check()
            return _reason
        }

    @Synchronized
    fun check() {
        if (checked) return
        checked = true

        if (System.getProperty("OYSM_DISABLE_GPU") != null) {
            _reason = "gpu renderer has been disabled"
            return
        }
        if (!NativeLibLoader.isLoaded) {
            _reason = "native ysm-core not loaded"
            return
        }
        val osName = System.getProperty("os.name", "").lowercase(Locale.ROOT)
        if (osName.contains("mac") || osName.contains("darwin")) {
            _reason = "macOS GL is capped at 4.1 and lacks GL_ARB_shader_storage_buffer_object"
            return
        }

        val caps: GLCapabilities
        val glVersion: String?
        val glRenderer: String?
        val glVendor: String?
        val glslVersion: String?
        try {
            RenderSystem.assertOnRenderThread()
            caps = GL.getCapabilities()
            glVersion = GL11.glGetString(GL11.GL_VERSION)
            glRenderer = GL11.glGetString(GL11.GL_RENDERER)
            glVendor = GL11.glGetString(GL11.GL_VENDOR)
            glslVersion = GL11.glGetString(0x8B8C)
        } catch (t: Throwable) {
            _reason = "GL capabilities not available: ${t.message}"
            return
        }

        if (glVersion == null) {
            _reason = "GL version not available"
            return
        }

        Constants.LOGGER.info("OpenGL version: {}", glVersion)
        Constants.LOGGER.info("OpenGL renderer version: {}", glRenderer)
        Constants.LOGGER.info("OpenGL vendor: {}", glVendor)
        Constants.LOGGER.info("OpenGL glsl version: {}", glslVersion)

        if (!caps.OpenGL30) {
            _reason = "OpenGL 3.0 not supported (got $glVersion)"
            return
        }

        val hasSsbo = caps.OpenGL43 || caps.GL_ARB_shader_storage_buffer_object
        val hasIfaceQuery = caps.OpenGL43 || caps.GL_ARB_program_interface_query
        val hasLayoutBinding = caps.OpenGL42 || caps.GL_ARB_shading_language_420pack
        val hasExplicitAttrib = caps.OpenGL33 || caps.GL_ARB_explicit_attrib_location
        val hasPackedNormal = caps.OpenGL33 || caps.GL_ARB_vertex_type_2_10_10_10_rev
        if (!hasSsbo) {
            _reason = "SSBO not supported, GL_VERSION=$glVersion"
            return
        }
        if (!hasIfaceQuery) {
            _reason = "GL_ARB_program_interface_query not supported; GL_VERSION=$glVersion"
            return
        }
        if (!hasLayoutBinding) {
            _reason = "GL_ARB_shading_language_420pack not supported; GL_VERSION=$glVersion"
            return
        }
        if (!hasExplicitAttrib) {
            _reason = "GL_ARB_explicit_attrib_location not supported; GL_VERSION=$glVersion"
            return
        }
        if (!hasPackedNormal) {
            _reason = "GL_ARB_vertex_type_2_10_10_10_rev not supported; GL_VERSION=$glVersion"
            return
        }

        available = true
        _reason = "ok (GL $glVersion, $glRenderer)"
    }
}