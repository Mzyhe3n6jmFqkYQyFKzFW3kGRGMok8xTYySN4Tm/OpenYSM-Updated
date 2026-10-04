@file:Suppress("MemberVisibilityCanBePrivate")

package rip.ysm.gui

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

    open fun getLabel(): Component = Component.translatable("gui.yes_steve_model.config.$translationKey")

    open fun getDescription(): Component = Component.translatable("gui.yes_steve_model.config.$translationKey.desc")

    open fun get(): T = pendingValue

    open fun setPending(value: T) {
        pendingValue = value
        dirty = !Objects.equals(value, getter())
    }

    open fun isDirty(): Boolean {
        return dirty
    }

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
        @JvmStatic
        fun ofBoolean(key: String, cfg: ForgeConfigSpec.BooleanValue): Option<Boolean> =
            Option(key, { cfg.get() }, { cfg.set(it); runCatching { cfg.save() } })

        @JvmStatic
        fun ofDouble(key: String, cfg: ForgeConfigSpec.DoubleValue): Option<Double> =
            Option(key, { cfg.get() }, { cfg.set(it); runCatching { cfg.save() } })

        @JvmStatic
        fun <E : Enum<E>> ofEnum(key: String, cfg: ForgeConfigSpec.EnumValue<E>): Option<E> =
            Option(key, { cfg.get() }, { cfg.set(it); runCatching { cfg.save() } })
    }
}