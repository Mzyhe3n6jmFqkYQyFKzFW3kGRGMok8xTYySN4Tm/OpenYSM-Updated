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
        when {
            path.isDirectory -> {
                val files = path.listFiles() ?: return
                for (file in files) {
                    when {
                        file.isDirectory -> {
                            findFiles(file, filter, list)
                        }

                        filter(file) -> {
                            list.add(file)
                        }
                    }
                }
            }

            filter(path) -> {
                list.add(path)
            }
        }
    }
}
