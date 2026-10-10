@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.elfmcys.yesstevemodel

import net.minecraft.network.chat.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.*

object NativeLibLoader {
    private const val NATIVE_DIR = "natives"

    @Volatile
    private var available = false

    @Volatile
    private var loaded = false

    @Volatile
    private var isAndroid = false

    @JvmStatic
    val isAvailable: Boolean
        get() = available

    @JvmStatic
    val isLoaded: Boolean
        get() = loaded

    @JvmStatic
    val isOnAndroid: Boolean
        get() = isAndroid

    private var lastError: ErrorState? = null

    private data class ErrorState(
        val component: Component,
        val logMsg: String
    )

    private data class PlatformInfo(
        var osTag: String = "",
        var archTag: String = "",
        var folder: String? = null,
        var libraryName: String? = null
    )

    @Synchronized
    @JvmStatic
    fun init() {
        if (available) return

        if (System.getProperty("OYSM_DISABLE_SMID") != null) {
            available = true
            loaded = false
            return
        }

        val platform = detectPlatform()
        val libName = platform.libraryName
        val folder = platform.folder
        if (libName == null || folder == null) {
            setUnsupportedPlatformError("${platform.osTag} ${platform.archTag}")
            available = true
            loaded = false
            return
        }

        val resourcePath = "$NATIVE_DIR/$folder/$libName"
        val classLoader = NativeLibLoader::class.java.classLoader
            ?: ClassLoader.getSystemClassLoader()

        val inStream = classLoader?.getResourceAsStream(resourcePath)
        if (inStream == null) {
            Constants.LOGGER.warn("Native library not found in JAR: {}", resourcePath)
            setUnsatisfiedRuntimeError("Native library not found in JAR: $resourcePath")
            loaded = false
            available = true
            return
        }

        runCatching {
            val tempDir: Path = Files.createTempDirectory("ysm_native_")
            tempDir.toFile().deleteOnExit()

            val extractedLib: Path = tempDir.resolve(libName)

            inStream.use { stream ->
                Files.copy(stream, extractedLib, StandardCopyOption.REPLACE_EXISTING)
            }

            if (!platform.osTag.contains("win")) {
                extractedLib.toFile().setExecutable(true)
            }

            val start = System.currentTimeMillis()
            Constants.LOGGER.info("Begin load native library")
            System.load(extractedLib.toAbsolutePath().toString())
            Constants.LOGGER.info("Successfully load native library in {}ms", System.currentTimeMillis() - start)

            extractedLib.toFile().deleteOnExit()

            loaded = true
            available = true
        }.onFailure { th ->
            Constants.LOGGER.error("Failed to load native lib: $resourcePath", th)
            setUnsatisfiedRuntimeError(th.message ?: th.toString())
            loaded = false
            available = true
        }
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
                        info.libraryName = "ysm-core.dll"
                    }

                    arch.contains("x86") || arch.contains("i386") -> {
                        info.folder = "windows-x86"
                        info.libraryName = "ysm-core.dll"
                    }
                }
            }

            os.contains("mac") || os.contains("darwin") -> {
                when {
                    arch == "arm64" || arch.contains("aarch64") -> {
                        info.folder = "macos-arm64"
                        info.libraryName = "libysm-core.dylib"
                    }

                    arch == "x64" || arch.contains("amd64") -> {
                        info.folder = "macos-x64"
                        info.libraryName = "libysm-core.dylib"
                    }
                }
            }

            os.contains("linux") -> {
                when {
                    arch == "x64" || arch.contains("amd64") -> {
                        info.folder = "linux-x64"
                        info.libraryName = "libysm-core.so"
                    }

                    arch == "arm64" || arch.contains("aarch64") -> {
                        info.folder = "android-arm64"
                        info.libraryName = "libysm-core.so"
                        isAndroid = true
                    }
                }
            }
        }

        return info
    }

    private fun setUnsupportedPlatformError(info: String) {
        lastError = ErrorState(
            Component.translatable("error.yes_steve_model.unsupported_platform", info),
            "[YSM] Unsupported platform: $info"
        )
    }

    private fun setUnsatisfiedRuntimeError(msg: String) {
        lastError = ErrorState(
            Component.translatable("error.yes_steve_model.unsatisfied_runtime_env", msg),
            "[YSM] Runtime error: $msg"
        )
    }

    @JvmStatic
    val errorComponent: Component?
        get() = lastError?.component

    @JvmStatic
    val errorMessage: String?
        get() = lastError?.logMsg
}
