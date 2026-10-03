package rip.ysm.gui

import net.minecraft.network.chat.Component
import net.minecraftforge.common.ForgeConfigSpec
import java.util.Objects

open class Option<T>(
    val translationKey: String,
    val getter: () -> T,
    val setter: (T) -> Unit
) {
    var pending: T = getter()
    var dirty: Boolean = false

    open fun getLabel(): Component {
        return Component.translatable("gui.yes_steve_model.config.$translationKey")
    }

    open fun getDescription(): Component {
        return Component.translatable("gui.yes_steve_model.config.$translationKey.desc")
    }

    open fun get(): T {
        return pending
    }

    open fun setPending(value: T) {
        this.pending = value
        this.dirty = !Objects.equals(value, getter())
    }

    open fun isDirty(): Boolean {
        return dirty
    }

    open fun apply() {
        if (dirty) {
            setter(pending)
            dirty = false
        }
    }

    open fun undo() {
        this.pending = getter()
        this.dirty = false
    }

    companion object {
        @JvmStatic
        fun ofBoolean(key: String, cfg: ForgeConfigSpec.BooleanValue): Option<Boolean> {
            return Option(key, { cfg.get() }, { cfg.set(it) })
        }

        @JvmStatic
        fun ofDouble(key: String, cfg: ForgeConfigSpec.DoubleValue): Option<Double> {
            return Option(key, { cfg.get() }, { cfg.set(it) })
        }

        @JvmStatic
        fun <E : Enum<E>> ofEnum(key: String, cfg: ForgeConfigSpec.EnumValue<E>): Option<E> {
            return Option(key, { cfg.get() }, { cfg.set(it) })
        }
    }
}