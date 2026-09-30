package com.elfmcys.yesstevemodel.util

import java.io.File

object FileFinder {
    @JvmStatic
    fun findFiles(path: File, filter: (File) -> Boolean): List<File> {
        val files = mutableListOf<File>()
        findFiles(path, filter, files)
        return files
    }

    private fun findFiles(path: File, filter: (File) -> Boolean, list: MutableList<File>) {
        if (path.isDirectory) {
            val files = path.listFiles() ?: return
            for (file in files) {
                if (file.isDirectory) {
                    findFiles(file, filter, list)
                } else if (filter(file)) {
                    list.add(file)
                }
            }
        } else if (filter(path)) {
            list.add(path)
        }
    }
}
