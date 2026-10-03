@file:Suppress("unused")

package rip.ysm.api.event

class EventResult(
    private val interrupts: Boolean,
    private val value: Boolean?
) {
    fun interrupts(): Boolean = interrupts
    fun isTrue(): Boolean = value == true
    fun isFalse(): Boolean = value == false
    fun value(): Boolean? = value

    companion object {
        @JvmField
        val PASS: EventResult = EventResult(false, null)

        @JvmField
        val INTERRUPT_TRUE: EventResult = EventResult(true, true)

        @JvmField
        val INTERRUPT_FALSE: EventResult = EventResult(true, false)

        @JvmStatic
        fun pass(): EventResult = PASS

        @JvmStatic
        fun interruptTrue(): EventResult = INTERRUPT_TRUE

        @JvmStatic
        fun interruptFalse(): EventResult = INTERRUPT_FALSE

        @JvmStatic
        fun interrupt(value: Boolean): EventResult = if (value) INTERRUPT_TRUE else INTERRUPT_FALSE
    }
}