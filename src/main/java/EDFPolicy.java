import Exceptions.NegativeTickValueException;
import Exceptions.RunOutOfTasksException;
import Exceptions.SchedulabilityViolationException;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Contains logic regarding Earliest Deadline First policy
 * */

// BUG  edf policy does not track which objects have been executed
// neither does it check if the acceptedQueue is empty

public class EDFPolicy {

    private ArrayList<Task> pendingTasks;
    private EDFTasksContainer acceptedTasks;
    private LazyTicker ticker;
    private Logger logger;
    Task currentTask = null;

    EDFPolicy(LazyTicker ticker, Logger logger) {

        this.ticker = ticker;
        this.logger = logger;
        acceptedTasks = new EDFTasksContainer();
        pendingTasks = new ArrayList<>();
    }

    void acceptTasks(ArrayList<Task> tasks) {

        for (Task task : tasks) {
            acceptTasks(task);
        }
    }

    void acceptTasks(Task task) {
        pendingTasks.add(task);
    }

    void enforceSchedulability() throws SchedulabilityViolationException {

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

    /**
     * ssmart scheduler will select a subset of tasks*/

    private void tryConfirmNewTasks() {

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

    private void rePickTaskOnDemand() {

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

    private void nextTick() {

        ticker.nextTick();
        logger.info("Tick happened - current time is " + ticker.getTicksCounter() + " ticks");
    }

    void executeTask() {

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

    void nextStep() throws RunOutOfTasksException {

        tryConfirmNewTasks();

        rePickTaskOnDemand();

        executeTask();

        nextTick();
    }
}
