@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.elfmcys.yesstevemodel

import com.sun.jna.NativeLibrary
import net.minecraft.network.chat.Component
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.SystemUtils
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path

object NativeLibLoader {
    private var available = false
    private var loaded = false
    private var isAndroid = false

    @JvmStatic
    fun isAvailable(): Boolean = available

    @JvmStatic
    fun isLoaded(): Boolean = loaded

    @JvmStatic
    fun isOnAndroid(): Boolean = isAndroid

    private var lastError: ErrorState? = null

    private enum class TargetPlatform(
        val resDir: String,
        val fileName: String,
        val defaultStorage: Path?
    ) {
        WINDOWS_X64("windows-x64", "ysm-core.dll", Path.of(System.getProperty("java.io.tmpdir"), "ysm")),
        WINDOWS_X86("windows-x86", "ysm-core.dll", Path.of(System.getProperty("java.io.tmpdir"), "ysm")),
        LINUX_X64("linux-x64", "libysm-core.so", Path.of(System.getProperty("user.home"), ".ysm")),
        MACOS_X64("macos-x64", "libysm-core.dylib", Path.of(System.getProperty("user.home"), ".ysm")),
        MACOS_ARM64("macos-arm64", "libysm-core.dylib", Path.of(System.getProperty("user.home"), ".ysm")),
        ANDROID_ARM64("android-arm64", "libysm-core.so", null);

        val resourcePath: String
            get() = "/natives/$resDir/$fileName"
    }

    private enum class LibcType {
        UNSUPPORTED, GNU, BIONIC
    }

    private data class ErrorState(
        val component: Component,
        val key: String?,
        val args: Array<Any>?,
        val logMsg: String
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ErrorState) return false
            if (component != other.component) return false
            if (key != other.key) return false
            if (args != null) {
                if (other.args == null || !args.contentEquals(other.args)) return false
            } else if (other.args != null) return false
            return logMsg == other.logMsg
        }

        override fun hashCode(): Int {
            var result = component.hashCode()
            result = 31 * result + key.hashCode()
            result = 31 * result + (args?.contentHashCode() ?: 0)
            result = 31 * result + logMsg.hashCode()
            return result
        }
    }

    @Throws(IOException::class)
    @JvmStatic
    fun init() {
        var path = System.getenv("YSM_CORE_LIB")
        if (path.isNullOrEmpty()) {
            path = extractAndGetLibPath()
        }

        if (path != null && loadNativeLib(path)) {
            loaded = true
        }
        available = true
    }

    @Throws(IOException::class)
    private fun extractAndGetLibPath(): String? {
        val platform = resolvePlatform() ?: return null
        var storageDir = platform.defaultStorage
        if (platform == TargetPlatform.ANDROID_ARM64) {
            val androidRuntime = System.getenv("MOD_ANDROID_RUNTIME")
            if (androidRuntime == null) {
                setUnsupportedLauncherError()
                return null
            }
            isAndroid = true
            storageDir = Path.of(androidRuntime)
        }

        val data = readResource(platform.resourcePath)
        if (data == null) {
            setUnsatisfiedBuildError()
            return null
        }

        val validStorageDir = storageDir ?: return null
        val targetFile = ensureDirectory(validStorageDir).resolve(platform.fileName).toAbsolutePath().normalize()
        val finalPath = targetFile.toString()

        writeIfChanged(finalPath, data)
        return finalPath
    }

    private fun resolvePlatform(): TargetPlatform? {
        val isX86_64 = SystemUtils.OS_ARCH == "amd64" || SystemUtils.OS_ARCH == "x86_64"
        val isAarch64 = SystemUtils.OS_ARCH == "aarch64"

        if (SystemUtils.IS_OS_WINDOWS) {
            return if (isX86_64) TargetPlatform.WINDOWS_X64 else TargetPlatform.WINDOWS_X86
        }

        if (SystemUtils.IS_OS_LINUX) {
            val libc = detectLibcType()
            if (libc == LibcType.GNU) return if (isX86_64) TargetPlatform.LINUX_X64 else null
            if (libc == LibcType.BIONIC) return if (isAarch64) TargetPlatform.ANDROID_ARM64 else null
            setUnsupportedPlatformError("Linux (Unknown Libc)")
            return null
        }

        if (SystemUtils.IS_OS_MAC) {
            if (isAarch64) return TargetPlatform.MACOS_ARM64
            if (isX86_64) return TargetPlatform.MACOS_X64
            setUnsupportedPlatformError("macOS (Unsupported Architecture: ${SystemUtils.OS_ARCH})")
            return null
        }

        setUnsupportedPlatformError("${SystemUtils.OS_NAME} ${SystemUtils.OS_ARCH}")
        return null
    }

    private fun loadNativeLib(path: String): Boolean {
        return System.getProperty("OYSM_DISABLE_SMID") == null && runCatching {
            val start = System.currentTimeMillis()
            Constants.LOGGER.info("Begin load native library")
            System.load(path)
            Constants.LOGGER.info("Successfully load native library in {}ms", System.currentTimeMillis() - start)
            true
        }.onFailure { th ->
            Constants.LOGGER.error("Failed to load native lib: $path", th)
            setUnsatisfiedRuntimeError(th.message ?: th.toString())
        }.getOrDefault(false)
    }

    @Throws(IOException::class)
    private fun writeIfChanged(path: String, data: ByteArray) {
        val file = File(path)
        if (file.exists() && file.length() == data.size.toLong()) {
            val existing = FileUtils.readFileToByteArray(file)
            if (data.contentEquals(existing)) return
        }
        FileUtils.writeByteArrayToFile(file, data, false)
    }

    @Throws(IOException::class)
    private fun readResource(path: String): ByteArray? {
        val url: URL? = NativeLibLoader::class.java.getResource(path) ?: YesSteveModel::class.java.getResource(path)
        if (url == null) return null
        return runCatching {
            url.openStream().use { `is`: InputStream ->
                IOUtils.toByteArray(`is`)
            }
        }.getOrNull()
    }

    private fun ensureDirectory(path: Path): Path = runCatching {
        if (!Files.isDirectory(path)) Files.createDirectories(path)
        path
    }.getOrElse {
        Constants.ConfigDir.resolve("cache")
    }

    private fun detectLibcType(): LibcType = runCatching {
        val libc = NativeLibrary.getInstance(com.sun.jna.Platform.C_LIBRARY_NAME)
        if (libc != null) {
            if (hasFunction(libc, "android_set_abort_message")) return@runCatching LibcType.BIONIC
            if (hasFunction(libc, "gnu_get_libc_version")) return@runCatching LibcType.GNU
        }
        LibcType.UNSUPPORTED
    }.getOrDefault(LibcType.UNSUPPORTED)

    private fun hasFunction(lib: NativeLibrary, name: String): Boolean {
        return runCatching {
            lib.getFunction(name) != null
        }.getOrDefault(false)
    }

    private fun setUnsupportedPlatformError(detail: String?) {
        val info = detail ?: "${SystemUtils.OS_NAME} ${SystemUtils.OS_ARCH}"
        lastError = ErrorState(
            Component.translatable("error.yes_steve_model.unsupported_platform", info),
            "error.yes_steve_model.unsupported_platform_ext",
            arrayOf(info),
            "[YSM] Unsupported platform: $info"
        )
    }

    private fun setUnsatisfiedRuntimeError(msg: String) {
        lastError = ErrorState(
            Component.translatable("error.yes_steve_model.unsatisfied_runtime_env", msg),
            "error.yes_steve_model.unsatisfied_runtime_env_ext",
            arrayOf(msg),
            "[YSM] Runtime error: $msg"
        )
    }

    private fun setUnsatisfiedBuildError() {
        val info = "${SystemUtils.OS_NAME} ${SystemUtils.OS_ARCH}"
        lastError = ErrorState(
            Component.translatable("error.yes_steve_model.unsatisfied_build", info),
            "error.yes_steve_model.unsatisfied_build_ext",
            arrayOf(info),
            "[YSM] No build for platform: $info"
        )
    }

    private fun setUnsupportedLauncherError() {
        val fcl = System.getenv("FCL_VERSION_CODE")
        if (StringUtils.isNotBlank(fcl)) {
            lastError = createLauncherError("FCL", "1.2.6.7")
            return
        }
        val zalith = System.getenv("ZALITH_VERSION_CODE")
        if (StringUtils.isNotBlank(zalith)) {
            val ver = zalith.toIntOrNull() ?: 0
            lastError = if (ver < 190000) createLauncherError("Zalith 1", "1.4.1.1") else createLauncherError(
                "Zalith 2",
                "2.0.0_beta-20251118a"
            )
            return
        }
        lastError = ErrorState(
            Component.translatable("error.yes_steve_model.unsupported_launcher"),
            null,
            null,
            "[YSM] Unsupported Launcher"
        )
    }

    private fun createLauncherError(name: String, minVer: String): ErrorState {
        return ErrorState(
            Component.translatable("error.yes_steve_model.old_launcher", name, minVer),
            "error.yes_steve_model.old_launcher_ext",
            arrayOf(name, minVer),
            "[YSM] Old launcher version: $name"
        )
    }

    @JvmStatic
    fun getErrorComponent(): Component? = lastError?.component

    @JvmStatic
    fun getErrorMessage(): String? = lastError?.logMsg
}
