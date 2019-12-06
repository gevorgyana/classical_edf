/*
* in the tests, it may be needed
* to run scheduler only up to a point and
* when a user (test class) want to
* */

/**
 * Implements producer consumer pattern;
 * buffer is of size 1
 * */

public class ManualTickTimer {

    private int ticksCounter = 0;
    boolean tickHasBeenConsumed = false;

    // producer
    public synchronized void tick() {
        if (tickHasBeenConsumed == false) {
            // noop
        }
        ++ticksCounter;
    }

    // consumer
    public synchronized int getTicksCounter() {
        return ticksCounter;
    }
}
