package com.elfmcys.yesstevemodel.molang.parser

import com.elfmcys.yesstevemodel.molang.lexer.Cursor
import java.io.IOException

open class ParseException : IOException {
    val cursor: Cursor?

    constructor(cursor: Cursor?) : super() {
        this.cursor = cursor
    }

    constructor(str: String, cursor: Cursor?) : super(appendCursor(str, cursor)) {
        this.cursor = cursor
    }

    constructor(th: Throwable, cursor: Cursor?) : super(th) {
        this.cursor = cursor
    }

    constructor(str: String, th: Throwable, cursor: Cursor?) : super(appendCursor(str, cursor), th) {
        this.cursor = cursor
    }

    fun cursor(): Cursor? = cursor

    companion object {
        private fun appendCursor(message: String, cursor: Cursor?): String {
            if (cursor == null) return message
            return "$message\n  at $cursor"
        }
    }
}