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
        val PASS: EventResult = EventResult(false, null)

        val INTERRUPT_TRUE: EventResult = EventResult(true, true)

        val INTERRUPT_FALSE: EventResult = EventResult(true, false)

        fun pass(): EventResult = PASS

        fun interruptTrue(): EventResult = INTERRUPT_TRUE

        fun interruptFalse(): EventResult = INTERRUPT_FALSE

        fun interrupt(value: Boolean): EventResult = if (value) INTERRUPT_TRUE else INTERRUPT_FALSE
    }
}