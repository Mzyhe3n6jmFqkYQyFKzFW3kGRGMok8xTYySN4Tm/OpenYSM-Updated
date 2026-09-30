package rip.ysm.api.event;

public final class EventResult {
    private static final EventResult PASS = new EventResult(false, null);
    private static final EventResult INTERRUPT_TRUE = new EventResult(true, Boolean.TRUE);
    private static final EventResult INTERRUPT_FALSE = new EventResult(true, Boolean.FALSE);

    private final boolean interrupts;
    private final Boolean value;

    private EventResult(boolean interrupts, Boolean value) {
        this.interrupts = interrupts;
        this.value = value;
    }

    public static EventResult pass() {
        return PASS;
    }

    public static EventResult interruptTrue() {
        return INTERRUPT_TRUE;
    }

    public static EventResult interruptFalse() {
        return INTERRUPT_FALSE;
    }

    public static EventResult interrupt(boolean value) {
        return value ? INTERRUPT_TRUE : INTERRUPT_FALSE;
    }

    public boolean interrupts() {
        return interrupts;
    }

    public boolean isTrue() {
        return Boolean.TRUE.equals(value);
    }

    public boolean isFalse() {
        return Boolean.FALSE.equals(value);
    }
}
