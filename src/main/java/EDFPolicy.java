import Exceptions.NegativeTickValueException;
import Exceptions.RunOutOfTasksException;
import Exceptions.SchedulabilityViolationException;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Contains logic regarding Earliest Deadline First policy
 * */

public class EDFPolicy {

    private ArrayList<Task> pendingTasks;
    private EDFTasksContainer acceptedTasks;
    private LazyTicker ticker;
    private Logger logger;
    private Task currentTask = null;

    // todo this is ugly, but think about it later
    // private
    public int endOfExecutionHint = -1;

    EDFPolicy(LazyTicker ticker, Logger logger) {

        this.ticker = ticker;
        this.logger = logger;
        acceptedTasks = new EDFTasksContainer();
        pendingTasks = new ArrayList<>();
    }

    public void acceptTasks(ArrayList<Task> tasks) {

        for (Task task : tasks) {
            acceptTasks(task);
        }
    }

    // todo this is ugly, but i have to do it: we have to store
    // variable which may be not needed at all if we are
    // willing to run forever, for example, while (true) with no break;
    // there is no better place to store it in as for the architecture at present
    public void acceptSimulationTimeout(int endOfExecutionHint) {
        this.endOfExecutionHint = endOfExecutionHint;
    }

    public void acceptTasks(Task task) {
        pendingTasks.add(task);
    }

    public void enforceSchedulability() throws SchedulabilityViolationException {

        if (!pendingTasks.isEmpty()) {

            EDFTasksContainer queueUnderTest = new EDFTasksContainer();
            for (Task task : pendingTasks) {
                queueUnderTest.add(task);
            }

            boolean feasible = true;
            int ticksNeeded = 0;

            for (Task task : queueUnderTest) {
                ticksNeeded += task.getWorstCaseRunningTime();
                feasible &= (ticksNeeded <= (task.getAbsoluteDeadline()));
            }

            if (feasible) {
                acceptedTasks = queueUnderTest;
                pendingTasks.clear();
                return;
            }

            throw new SchedulabilityViolationException();
        }
    }

    // private
    public void tryConfirmNewTasks() {

        if (!pendingTasks.isEmpty()) {

            try {

                enforceSchedulability();
                logger.info("All pending tasks have been accepted");
            }

            catch (SchedulabilityViolationException e) {

                logger.warning("Accepting pending tasks will make the system not schedulable;" +
                        " all pending tasks are discarded");
            }
        }
    }

    //private
    public void rePickTaskOnDemand() {

        if (currentTask == null) {

            if (acceptedTasks.isEmpty()) {

                logger.warning("The system does not do anything");
                throw new RunOutOfTasksException();

            } else {

                currentTask = acceptedTasks.getNextTask();
                logger.info("Task #" + currentTask.getTaskID() + " has been picked");
            }
        }
    }

    //private
    public void nextTick() {

        ticker.nextTick();
        logger.info("Tick happened - current time is " + ticker.getTicksCounter() + " ticks");
    }

    public void executeTask() {

        if (currentTask != null) {

            try {
                currentTask.consumeTick();
                logger.info("Spending current time slice on task #" + currentTask.getTaskID());
            }

            catch (NegativeTickValueException e) {
                logger.info("Task #" + currentTask.getTaskID() + " has finished");
                currentTask = null;
            }
        }
    }

    /**
     * bug exception should be handled in the scheduler
     * because otherwise there will b no progress made, if there is
     * no task at current position in time
     * */

    public void nextStep() throws RunOutOfTasksException {

        // currently, when working with tick handler,
        // the system will only know about the tasks
        // at the end of the first invocation of this method

        nextTick();

        tryConfirmNewTasks();

        try {
            rePickTaskOnDemand();
        } catch (RunOutOfTasksException e) {
            // we know we are not doing any work,
            // but there mat be more work soon
        }

        executeTask();


    }
}
