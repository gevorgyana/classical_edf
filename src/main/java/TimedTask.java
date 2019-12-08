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
