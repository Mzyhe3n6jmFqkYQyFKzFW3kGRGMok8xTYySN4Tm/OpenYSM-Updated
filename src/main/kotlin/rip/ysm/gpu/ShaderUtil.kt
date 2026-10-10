package rip.ysm.gpu

import org.lwjgl.opengl.GL20
import java.io.IOException
import java.nio.charset.StandardCharsets

object ShaderUtil {
    @Throws(IOException::class)
    fun loadResource(path: String): String {
        val stream = ShaderUtil::class.java.getResourceAsStream(path)
            ?: throw IOException("resource not found: $path")
        return stream.use {
            it.bufferedReader(StandardCharsets.UTF_8).readText()
        }
    }

    fun compileShader(glType: Int, src: String, name: String): Int {
        val sh = GL20.glCreateShader(glType)
        GL20.glShaderSource(sh, src)
        GL20.glCompileShader(sh)
        if (GL20.glGetShaderi(sh, GL20.GL_COMPILE_STATUS) == 0) {
            val log = GL20.glGetShaderInfoLog(sh)
            GL20.glDeleteShader(sh)
            throw RuntimeException("Compile failed ($name): $log")
        }
        return sh
    }

    @Throws(IOException::class)
    fun compileShaderFromResource(glType: Int, resourcePath: String): Int {
        return compileShader(glType, loadResource(resourcePath), resourcePath)
    }

    fun linkProgram(vararg shaderIds: Int): Int {
        return linkProgramWith(null, *shaderIds)
    }

    fun linkProgramWith(preLink: ((Int) -> Unit)?, vararg shaderIds: Int): Int {
        val prog = GL20.glCreateProgram()
        for (sh in shaderIds) {
            GL20.glAttachShader(prog, sh)
        }
        preLink?.invoke(prog)
        GL20.glLinkProgram(prog)
        if (GL20.glGetProgrami(prog, GL20.GL_LINK_STATUS) == 0) {
            val log = GL20.glGetProgramInfoLog(prog)
            GL20.glDeleteProgram(prog)
            for (sh in shaderIds) {
                GL20.glDeleteShader(sh)
            }
            throw RuntimeException("Link failed: $log")
        }
        for (sh in shaderIds) {
            GL20.glDetachShader(prog, sh)
            GL20.glDeleteShader(sh)
        }
        return prog
    }
}