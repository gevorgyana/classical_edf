/* A tick generator, use one such instance per
* simulation */

public class LazyTicker {

    private int ticksCounter = 0;

    public void nextTick() {
        ++ticksCounter;
    }

    public int getTicksCounter() {
        return ticksCounter;
    }
}
