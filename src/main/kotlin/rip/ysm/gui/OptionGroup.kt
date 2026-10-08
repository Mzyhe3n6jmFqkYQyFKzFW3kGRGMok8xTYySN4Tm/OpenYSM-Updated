package rip.ysm.gui

import net.minecraft.network.chat.Component

open class OptionGroup(val translationKey: String) {
    @JvmField
    val rows: MutableList<OptionRow<*>> = ArrayList()

    open fun getTitle(): Component =
        Component.translatable("gui.yes_steve_model.config.group.$translationKey")

    open fun add(row: OptionRow<*>): OptionGroup {
        rows.add(row)
        return this
    }

    open fun isDirty(): Boolean =
        rows.any { it.option?.isDirty == true }

    open fun apply() {
        for (row in rows) {
            row.option?.apply()
        }
    }

    open fun undo() {
        for (row in rows) {
            row.option?.undo()
            row.refresh()
        }
    }
}