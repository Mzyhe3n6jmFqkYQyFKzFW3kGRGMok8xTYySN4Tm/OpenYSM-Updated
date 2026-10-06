@file:Suppress("unused")

package com.ysm.parser

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.*

/**
 * Loads the YSMParser native shared library (.so/.dll/.dylib) from inside the
 * mod JAR.
 *
 * The library is extracted to a temporary directory on first call and loaded
 * via [System.load]. Subsequent calls are no-ops.
 *
 * Platforms that lack a JNI library are detected and reported via a
 * `false` return from [load].
 */
object YSMParserNativeLoader {
    private const val NATIVE_DIR = "natives/ysmparser"

    @Volatile
    private var loaded = false

    @Volatile
    private var jniAvailable = false

    private data class PlatformInfo(
        var osTag: String = "",
        var archTag: String = "",
        var folder: String? = null,
        var libraryName: String? = null
    )

    /**
     * Load the YSMParser JNI library for the current platform.
     * Thread-safe and idempotent — safe to call multiple times.
     *
     * @return `true` if JNI loaded successfully, `false` if JNI is
     *         unavailable (caller should skip native parsing)
     */
    @Synchronized
    @JvmStatic
    fun load(): Boolean {
        if (loaded) return jniAvailable

        val platform = detectPlatform()
        val libName = platform.libraryName
        if (libName == null) {
            loaded = true
            jniAvailable = false
            return false
        }

        val resourcePath = "$NATIVE_DIR/${platform.folder}/$libName"
        runCatching {
            val tempDir: Path = Files.createTempDirectory("ysm_native_")
            val extractedLib: Path = tempDir.resolve(libName)

            val classLoader = YSMParserNativeLoader::class.java.classLoader
                ?: ClassLoader.getSystemClassLoader()
            classLoader.getResourceAsStream(resourcePath).use { inStream ->
                if (inStream == null) {
                    throw IOException("Native library not found in JAR: $resourcePath")
                }
                Files.copy(inStream, extractedLib, StandardCopyOption.REPLACE_EXISTING)
            }

            if (!platform.osTag.contains("win")) {
                extractedLib.toFile().setExecutable(true)
            }

            System.load(extractedLib.toAbsolutePath().toString())

            extractedLib.toFile().deleteOnExit()
            tempDir.toFile().deleteOnExit()

            loaded = true
            jniAvailable = true
            return true
        }.getOrElse {
            loaded = true
            jniAvailable = false
            return false
        }
    }

    @Synchronized
    @JvmStatic
    fun isJniAvailable(): Boolean {
        if (!loaded) {
            val platform = detectPlatform()
            return platform.libraryName != null
        }
        return jniAvailable
    }

    private fun detectPlatform(): PlatformInfo {
        val os = System.getProperty("os.name", "").lowercase(Locale.ROOT)
        var arch = System.getProperty("os.arch", "").lowercase(Locale.ROOT)

        when (arch) {
            "amd64", "x86_64" -> {
                arch = "x64"
            }

            "aarch64", "arm64" -> {
                arch = "arm64"
            }
        }

        val info = PlatformInfo(osTag = os, archTag = arch)

        when {
            os.contains("win") -> {
                when {
                    arch == "x64" || arch.contains("amd64") -> {
                        info.folder = "windows-x64"
                        info.libraryName = "YSMParserJNI.dll"
                    }

                    arch.contains("x86") || arch.contains("i386") -> {
                        info.folder = "windows-x86"
                        info.libraryName = "YSMParserJNI.dll"
                    }
                }
            }

            os.contains("mac") || os.contains("darwin") -> {
                when {
                    arch == "arm64" || arch.contains("aarch64") -> {
                        info.folder = "macos-arm64"
                        info.libraryName = "libYSMParserJNI.dylib"
                    }
                }
                // x64 macOS not bundled — no JNI
            }

            os.contains("linux") -> {
                when {
                    arch == "x64" || arch.contains("amd64") -> {
                        info.folder = "linux-x64"
                        info.libraryName = "libYSMParserJNI.so"
                    }

                    arch == "arm64" || arch.contains("aarch64") -> {
                        info.folder = "linux-arm64"
                        info.libraryName = "libYSMParserJNI.so"
                    }
                }
                // loongarch64, riscv64 — no JNI
            }
        }

        return info
    }
}