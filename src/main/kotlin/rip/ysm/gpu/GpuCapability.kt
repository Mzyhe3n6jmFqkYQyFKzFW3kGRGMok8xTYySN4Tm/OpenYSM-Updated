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
    private var reason2: String? = null

    @JvmStatic
    val isAvailable: Boolean
        get() {
            if (!checked) check()
            return available
        }

    @JvmStatic
    val reason: String?
        get() {
            if (!checked) check()
            return reason2
        }

    @Synchronized
    @JvmStatic
    fun check() {
        if (checked) return
        checked = true

        if (System.getProperty("OYSM_DISABLE_GPU") != null) {
            reason2 = "gpu renderer has been disabled"
            return
        }
        if (!NativeLibLoader.isLoaded) {
            reason2 = "native ysm-core not loaded"
            return
        }
        val osName = System.getProperty("os.name", "").lowercase(Locale.ROOT)
        if (osName.contains("mac") || osName.contains("darwin")) {
            reason2 = "macOS GL is capped at 4.1 and lacks GL_ARB_shader_storage_buffer_object"
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
            reason2 = "GL capabilities not available: ${t.message}"
            return
        }

        if (glVersion == null) {
            reason2 = "GL version not available"
            return
        }

        Constants.LOGGER.info("OpenGL version: {}", glVersion)
        Constants.LOGGER.info("OpenGL renderer version: {}", glRenderer)
        Constants.LOGGER.info("OpenGL vendor: {}", glVendor)
        Constants.LOGGER.info("OpenGL glsl version: {}", glslVersion)

        if (!caps.OpenGL30) {
            reason2 = "OpenGL 3.0 not supported (got $glVersion)"
            return
        }

        val hasSsbo = caps.OpenGL43 || caps.GL_ARB_shader_storage_buffer_object
        val hasIfaceQuery = caps.OpenGL43 || caps.GL_ARB_program_interface_query
        val hasLayoutBinding = caps.OpenGL42 || caps.GL_ARB_shading_language_420pack
        val hasExplicitAttrib = caps.OpenGL33 || caps.GL_ARB_explicit_attrib_location
        val hasPackedNormal = caps.OpenGL33 || caps.GL_ARB_vertex_type_2_10_10_10_rev
        if (!hasSsbo) {
            reason2 = "SSBO not supported, GL_VERSION=$glVersion"
            return
        }
        if (!hasIfaceQuery) {
            reason2 = "GL_ARB_program_interface_query not supported; GL_VERSION=$glVersion"
            return
        }
        if (!hasLayoutBinding) {
            reason2 = "GL_ARB_shading_language_420pack not supported; GL_VERSION=$glVersion"
            return
        }
        if (!hasExplicitAttrib) {
            reason2 = "GL_ARB_explicit_attrib_location not supported; GL_VERSION=$glVersion"
            return
        }
        if (!hasPackedNormal) {
            reason2 = "GL_ARB_vertex_type_2_10_10_10_rev not supported; GL_VERSION=$glVersion"
            return
        }

        available = true
        reason2 = "ok (GL $glVersion, $glRenderer)"
    }
}