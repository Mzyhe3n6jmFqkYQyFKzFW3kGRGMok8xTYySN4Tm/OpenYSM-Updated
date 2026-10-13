package com.elfmcys.yesstevemodel.util

import com.elfmcys.yesstevemodel.data.DeepCopy
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds

class AutoSaveTest {
    private class Holder(var value: Int = 0) : DeepCopy<Holder> {
        override fun deepCopy(): Holder = Holder(value)
    }

    private class TestManager(path: Path) : AbstractManager<Holder>("auto_save_test.dat", Holder(), path, 50.milliseconds) {
        override fun loadData(): Holder = Holder()

        override fun writeTo(saveData: Holder, target: Path) {
            Files.writeString(target, saveData.value.toString())
        }

        fun mutate(value: Int) {
            data.value = value
        }
    }

    @Test
    fun testAutoSaveFlushesWithoutExplicitSave() {
        val dir = Files.createTempDirectory("ysm-autosave")
        val path = dir.resolve("auto_save_test.dat")
        val manager = TestManager(path)

        // No save() call: the periodic auto-save loop must pick this up on its own.
        manager.mutate(42)

        val deadline = System.currentTimeMillis() + 5_000
        var content: String? = null
        while (System.currentTimeMillis() < deadline) {
            if (Files.exists(path)) {
                content = Files.readString(path)
                if (content == "42") break
            }
            Thread.sleep(25)
        }
        assertEquals("42", content, "auto-save should flush periodically without an explicit save()")
    }

    @Test
    fun testAtomicWriteLeavesNoTempFile() {
        val dir = Files.createTempDirectory("ysm-autosave")
        val path = dir.resolve("auto_save_test.dat")
        val manager = TestManager(path)

        manager.mutate(7)
        manager.saveNow()

        assertEquals("7", Files.readString(path))
        assertFalse(Files.exists(path.resolveSibling("${path.fileName}.tmp")), "temporary file must be cleaned up")
    }
}
