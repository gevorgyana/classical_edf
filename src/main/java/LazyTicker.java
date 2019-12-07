/*
* A tick generator, use one such instance per
* simulation, it is not intended to be
* used with a Task, as the Scheduler should be
* able to decouple ticks from other actions
*
* it has 2 APIs:
* - you can avoid specifying a tick handler and
* manually associate ticks with actions that need
* to be performed on a scheduler entity or whatever
* other object;
*
* - you can also specify a special tick handler;
* if you do so, then every time a request for increasing tick
* is processed, the ticker will also  go into
* an entry point of the ticket handler - this way, you can do bound
* a specific action to every tick
*
* lazy = u have to ask it to increase the ticks counter
* manually
*  */

public class LazyTicker {

    private TickHandler handler = null;
    private int ticksCounter = 0;

    LazyTicker() {}
    LazyTicker(TickHandler handler) {
        this.handler = handler;
    }

    public void nextTick() {
        ++ticksCounter;
        if (handler != null) {
            handler.handleNextTick(ticksCounter);
        }
    }

    public int getTicksCounter() {
        return ticksCounter;
    }
}
