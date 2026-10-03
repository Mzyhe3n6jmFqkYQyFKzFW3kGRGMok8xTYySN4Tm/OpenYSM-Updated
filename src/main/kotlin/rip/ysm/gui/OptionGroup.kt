package rip.ysm.gui

import net.minecraft.network.chat.Component
import java.util.ArrayList
import java.util.Collections
import java.util.List

open class OptionGroup {
    var translationKey: String = null
    val rows: MutableList<OptionRow<*>> = ArrayList()
    constructor(translationKey: String) {
        this.translationKey = translationKey
    }
    open fun getTranslationKey(): String {
        return translationKey
    }
    open fun getTitle(): Component {
        return Component.translatable("gui.yes_steve_model.config.group." + translationKey)
    }
    open fun add(row: OptionRow<*>): OptionGroup {
        rows.add(row)
        return this
    }
    open fun getRows(): MutableList<OptionRow<*>> {
        return Collections.unmodifiableList(rows)
    }
    open fun isDirty(): Boolean {
        for (row in rows) {
            if (row.getOption() != null && row.getOption().isDirty()) {
                return true
            }
        }
        return false
    }
    open fun apply() {
        for (row in rows) {
            if (row.getOption() != null) {
                row.getOption().apply()
            }
        }
    }
    open fun undo() {
        for (row in rows) {
            if (row.getOption() != null) {
                row.getOption().undo()
            }
            row.refresh()
        }
    }
}