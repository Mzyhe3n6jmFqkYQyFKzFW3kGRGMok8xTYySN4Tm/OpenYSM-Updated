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

    private class TestManager(
        path: Path,
        isServerSide: Boolean = true
    ) : AbstractManager<Holder>("abstract_manager_test.dat", Holder(), path, 50.milliseconds, isServerSide) {
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

    private fun awaitContent(path: Path, expected: String) {
        val deadline = System.currentTimeMillis() + 5_000
        while (System.currentTimeMillis() < deadline) {
            if (Files.exists(path) && Files.readString(path) == expected) return
            Thread.sleep(25)
        }
        fail("expected '$expected' to be written to $path")
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

    @Test
    fun testClientAutoSaveRunsWithoutServer() {
        val path = tempPath()
        val manager = TestManager(path, isServerSide = false)

        // No save() call and no SERVER_STARTING: the lifetime auto-save must still flush this.
        manager.mutate(42)

        awaitContent(path, "42")
    }

    @Test
    fun testServerAutoSaveWaitsForServer() {
        val path = tempPath()
        val manager = TestManager(path, isServerSide = true)

        manager.mutate(99)
        Thread.sleep(300)

        assertFalse(Files.exists(path), "server-owned data must not be auto-saved while no server is running")
    }
}
