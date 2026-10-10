@file:Suppress("unused")

package com.ysm.parser

import com.elfmcys.yesstevemodel.Constants
import java.nio.file.Files
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

    private var loaded = false
    private var jniAvailable = false

    private data class PlatformInfo(
        var osTag: String = "",
        var archTag: String = "",
        var folder: String? = null,
        var libraryName: String? = null
    )

    init {
        load()
    }

    /**
     * Load the YSMParser JNI library for the current platform.
     * Thread-safe and idempotent — safe to call multiple times.
     *
     * @return `true` if JNI loaded successfully, `false` if JNI is
     *         unavailable (caller should skip native parsing)
     */
    fun load(): Boolean {
        if (loaded) return jniAvailable

        val platform = detectPlatform()
        val libName = platform.libraryName
        val folder = platform.folder
        if (libName == null || folder == null) {
            Constants.LOGGER.warn("Unsupported platform for YSMParser: {} {}", platform.osTag, platform.archTag)
            loaded = true
            jniAvailable = false
            return false
        }

        val resourcePath = "$NATIVE_DIR/$folder/$libName"
        val classLoader = YSMParserNativeLoader::class.java.classLoader
            ?: ClassLoader.getSystemClassLoader()

        val inStream = classLoader?.getResourceAsStream(resourcePath)
        if (inStream == null) {
            Constants.LOGGER.warn("Native library not found in JAR: {}", resourcePath)
            loaded = true
            jniAvailable = false
            return false
        }

        runCatching {
            val tempDir = Files.createTempDirectory("ysm_native_")
            tempDir.toFile().deleteOnExit()

            val extractedLib = tempDir.resolve(libName)

            inStream.use {
                Files.copy(it, extractedLib, StandardCopyOption.REPLACE_EXISTING)
            }

            if (!platform.osTag.contains("win")) extractedLib.toFile().setExecutable(true)

            val start = System.currentTimeMillis()
            Constants.LOGGER.info("Begin load YSMParser native library")
            System.load(extractedLib.toAbsolutePath().toString())
            Constants.LOGGER.info("Successfully load YSMParser native library in {}ms", System.currentTimeMillis() - start)

            extractedLib.toFile().deleteOnExit()

            loaded = true
            jniAvailable = true
            return true
        }.onFailure {
            Constants.LOGGER.warn("Failed to load YSMParser native lib: $resourcePath", it)
            loaded = true
            jniAvailable = false
            return false
        }

        return jniAvailable
    }

    val isJniAvailable: Boolean
        get() {
            if (!loaded) {
                val platform = detectPlatform()
                return platform.libraryName != null && platform.folder != null
            }
            return jniAvailable
        }

    val isAvailable: Boolean
        get() = isJniAvailable

    private fun detectPlatform(): PlatformInfo {
        val os = System.getProperty("os.name", "").lowercase(Locale.ROOT)
        var arch = System.getProperty("os.arch", "").lowercase(Locale.ROOT)

        when (arch) {
            "amd64", "x86_64" -> arch = "x64"
            "aarch64", "arm64" -> arch = "arm64"
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
