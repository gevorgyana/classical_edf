/**
 * todo
 * 1 - extend config - allow users to script their behaviour of
 * tasks
 *
 * 2 - as a side thing, (this is really not related to a scheduler),
 * implement random generation of xml files of special format - for demonstration
 * purposes
 * */

public class TimedTask {
    private Task taskImplementation;
    private int arrivalTime;

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getAbsoluteDeadline() {
        return taskImplementation.getAbsoluteDeadline();
    }

    public int getWorstCaseRuntime() {
        return taskImplementation.getWorstCaseRunningTime();
    }

    public Task getTaskImplementation() {
        return taskImplementation;
    }

    public TimedTask(Task taskImplementation, int arrivalTime) {
        this.taskImplementation = taskImplementation;
        this.arrivalTime = arrivalTime;
    }
}
