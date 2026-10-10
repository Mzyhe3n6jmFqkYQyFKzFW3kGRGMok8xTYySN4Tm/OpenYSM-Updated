@file:Suppress("MemberVisibilityCanBePrivate")

package rip.ysm.gui

import com.elfmcys.yesstevemodel.extensions.setAndSave
import net.minecraft.network.chat.Component
import net.minecraftforge.common.ForgeConfigSpec
import java.util.*

open class Option<T>(
    val translationKey: String,
    val getter: () -> T,
    val setter: (T) -> Unit
) {
    protected var pendingValue: T = getter()
    protected var dirty: Boolean = false

    open val label: Component
        get() = Component.translatable("gui.yes_steve_model.config.$translationKey")

    open val description: Component
        get() = Component.translatable("gui.yes_steve_model.config.$translationKey.desc")

    open var value: T
        get() = pendingValue
        set(value) {
            pendingValue = value
            dirty = !Objects.equals(value, getter())
        }

    open val isDirty: Boolean
        get() = dirty

    open fun apply() {
        if (dirty) {
            setter(pendingValue)
            dirty = false
        }
    }

    open fun undo() {
        pendingValue = getter()
        dirty = false
    }

    companion object {
        fun ofBoolean(key: String, cfg: ForgeConfigSpec.BooleanValue): Option<Boolean> =
            Option(key, { cfg.get() }, { cfg.setAndSave(it) })

        fun ofDouble(key: String, cfg: ForgeConfigSpec.DoubleValue): Option<Double> =
            Option(key, { cfg.get() }, { cfg.setAndSave(it) })

        fun <E : Enum<E>> ofEnum(key: String, cfg: ForgeConfigSpec.EnumValue<E>): Option<E> =
            Option(key, { cfg.get() }, { cfg.setAndSave(it) })
    }
}