package com.elfmcys.yesstevemodel.util

import com.elfmcys.yesstevemodel.data.DeepCopy
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds

class AbstractManagerTest {
    private class Holder(var value: Int = 0) : DeepCopy<Holder> {
        override fun deepCopy(): Holder = Holder(value)
    }

    private class TestManager(path: Path) :
        AbstractManager<Holder>("abstract_manager_test.dat", Holder(), path, 50.milliseconds) {
        override fun loadData(): Holder = Holder(Files.readString(dataPath).trim().toInt())

        override fun writeTo(saveData: Holder, target: Path) {
            Files.writeString(target, saveData.value.toString())
        }

        fun mutate(value: Int) {
            data.value = value
        }

        fun value(): Int = data.value
    }

    private fun tempPath(): Path {
        val dir = Files.createTempDirectory("ysm-abstract-manager")
        return dir.resolve("abstract_manager_test.dat")
    }

    @Test
    fun testAtomicWriteLeavesNoTempFile() {
        val path = tempPath()
        val manager = TestManager(path)

        manager.mutate(7)
        manager.saveNow()

        assertEquals("7", Files.readString(path))
        assertFalse(Files.exists(path.resolveSibling("${path.fileName}.tmp")), "temporary file must be cleaned up")
    }

    @Test
    fun testReloadReadsPersistedValue() {
        val path = tempPath()
        TestManager(path).apply {
            mutate(11)
            saveNow()
        }

        assertEquals(11, TestManager(path).value(), "a new manager must load the persisted value")
    }
}
