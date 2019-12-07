import Exceptions.NegativeTickValueException;

/**
 * Realtime task according to the Liu and Layland
 * */

public class Task {

    private int absoluteDeadline;
    private int worstCaseRunningTime;
    private int taskID;

    Task(int absoluteDeadline, int worstCaseRunningTime, TaskIDManager IDManager) {
        this.absoluteDeadline = absoluteDeadline;
        this.worstCaseRunningTime = worstCaseRunningTime;
        this.taskID = IDManager.nextID();
    }

    public int getAbsoluteDeadline() {
        return absoluteDeadline;
    }

    public int getWorstCaseRunningTime() {
        return worstCaseRunningTime;
    }

    public boolean isFinished() {
        return worstCaseRunningTime == 0;
    }

    public void consumeTick() {

        --worstCaseRunningTime;
        if (worstCaseRunningTime < 0) {
            throw new NegativeTickValueException();
        }
    }

    public int getTaskID() {
        return taskID;
    }
}
