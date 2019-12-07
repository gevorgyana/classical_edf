/*
* A tick generator, use one such instance per
* simulation, it is not intended to be
* used with a Task, as the Scheduler should be
* able to decouple ticks from other actions
*  */

public class LazyTicker {

    private int ticksCounter = 0;

    public void nextTick() {
        ++ticksCounter;
    }

    public int getTicksCounter() {
        return ticksCounter;
    }
}
