import java.util.ArrayList;

public class TickHandler {

    private EDFPolicy scheduler = null;

    private ArrayList<Task> nextToBeScheduled = null;
    private ArrayList<TimedTask> remainingTasks = null;

    public TickHandler(EDFPolicy scheduler, ArrayList<TimedTask> tasks) {
        this.scheduler = scheduler;
        this.remainingTasks = tasks;
    }

    private void filterTasks(int currentTicksCounter) {

        for (int i = 0; i < remainingTasks.size(); ++i) {
            if (remainingTasks.get(i).getArrivalTime() == currentTicksCounter) {
                nextToBeScheduled.add(remainingTasks.get(i).getTaskImplementation());
            }
        }
    }

    // todo this method should be an override of an abstract method
    public void handleNextTick(int currentTicksCounter) {
        filterTasks(currentTicksCounter);
        scheduler.acceptTasks(nextToBeScheduled);
        nextToBeScheduled.clear();
    }
}
