import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TickHandler {

    private EDFPolicy scheduler = null;

    private ArrayList<Task> nextToBeScheduled = null;
    private ArrayList<TimedTask> remainingTasks;

    private int getSimulationDeadline(ArrayList<TimedTask> tasks) {
        // todo tasks is empty?..
        return Collections.max(tasks, Comparator.comparingInt(TimedTask::getArrivalTime)).getArrivalTime();
    }

    public TickHandler(EDFPolicy scheduler, ArrayList<TimedTask> tasks) {
        this.scheduler = scheduler;
        this.remainingTasks = tasks;
        scheduler.acceptSimulationTimeout(getSimulationDeadline(tasks));
    }

    private void filterTasks(int currentTicksCounter) {

        // todo check that counter is non-negative

        if (nextToBeScheduled == null) {
            nextToBeScheduled = new ArrayList<>();
        }

        for (int i = 0; i < remainingTasks.size(); ++i) {
            if (remainingTasks.get(i).getArrivalTime() == currentTicksCounter) {
                nextToBeScheduled.add(remainingTasks.get(i).getTaskImplementation());
            }
        }

        nextToBeScheduled = nextToBeScheduled;
    }

    // todo this method should be an override of an abstract method
    public void handleNextTick(int currentTicksCounter) {
        filterTasks(currentTicksCounter);
        scheduler.acceptTasks(nextToBeScheduled);
        nextToBeScheduled.clear();
    }
}
