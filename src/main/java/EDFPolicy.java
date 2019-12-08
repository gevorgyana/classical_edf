/**
 * Earliest deadline first policy. Limitations and applications
 *
 * Note! The SCHED_DEADLINE found in the linux kernel uses CBS algorithm,
 * which is a solution to the soft real time scheduling problem; in this simulation, we
 * are working with hard real-time limitations, so the fact that much of what is said in the documentation
 * about CBS is not used in this simulation should not be confusing - there is no possibility for tasks
 * to be postponed or miss their deadlines!
 *
 * - no task is able to break its worst case execution time guarantee; a task is required to
 * obey its worst case promise, and the system makes sure that the tasks's deadline will be
 * respected; (it is possible to provide a mechanism to in the EDFPolicy class to enforce deadline obedience even if
 * some process misbehaves (this is not real time anymore!), but, generally speaking, it will require other technique
 * like CSB (Constant Bandwidth Server) and is out of the scope of this simulation)
 *
 * - currently, the notion of periodicity is not accepted; a task can run in sporadic mode only (once it executes,
 * it disappears); TODO this simulation may be easily extended to support periodic tasks; conceptually it is similar to
 * automated 'revival' of the task every PERIOD number of steps, and in equivalent to just creating a new
 * task by hand, therefore it has nothing to do with the algorithm (for description of periodic and sporadic tasks,
 * see documentation for deadline scheduler, section 3)
 *
 * - the tasks are not allowed to block in the middle of their execution; this is called Liu and Layland model of task
 * execution (for explanation of the difficulties the self-blocking tasks introduce, see Constant Bandwidth Server
 * Revisited, for example, here http://ceur-ws.org/Vol-1291/ewili14_5.pdf), also here
 * is my own explanation why this case is difficult
 *
 * Classical EDF does not deal with blocking in the middle of their execution tasks; and the Wikipedia is confusing by
 * not providing a link to the original article which explains that.
 *
 * The reason why the classical EDF cannot be applied (at least, without proper modifications) is that according to
 * the classical problem statement the system is only restricted on being able to finish the task before a specific
 * absolute point in time; in case we want to allow the tasks to block at well-defined intervals, the restriction on the
 * scheduler becomes much more harsh.
 *
 * Here is an example that demonstrates that the problem is more complex if we allow tasks to block at well-defined
 * points in time (I do not even consider the problem when a task is allowed to block during any time interval,
 * as it is becoming to make little sense to demand from a scheduler being able to guarantee hard real-time behaviour
 * in this case):
 *
 * The example:
 *
 * Case 1 (tasks can be executed at any point in time)
 * task 1 : (deadline at 6, runtime 3)
 * task 2 : (deadline at 6, runtime 3)
 *
 * we can run them as follows:
 * task 1 takes 3 points and returns, then task 2 takes 3 points and returns;
 *
 * Case 2 (restriction on when exactly tasks can execute)
 * task 1 : (deadline at 6, runtime 3 total, can execute at 1, 2, 4)
 * task 2 : (deadline at 6, runtime 3 total, can execute at 4, 5, 6)
 *
 * We cannot accept the tasks, as their execution times overlap and there is no time available to execute both of them,
 * while at point 3 we are making no progress. But the classical EDF would accept these tasks. A simple idea how to
 * extend the classical EDF check is by making sure that there is no time overlap, and even if there is, we are still
 * still have enough time to do the work before deadline. But this is a substantial modification of
 * the algorithm and is therefore not implemented here
 *
 * Also, there is a theoretical issue with this implementation, called priority inversion (in short, it happens when
 * a process with lower priority (earliest deadline in our case) blocks on some resource, and the task with higher
 * priority is forced to wait until the task with lower priority finishes),
 * which can be solved with a technique called priority inheritance, it is not considered in code
 * */

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
    // willing to run forever, for example, there is no better place
    // to store it now
    public void acceptSimulationTimeout(int endOfExecutionHint) {
        this.endOfExecutionHint = endOfExecutionHint;
    }

    public int getEndOfExecutionHint() {
        return endOfExecutionHint;
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
     * bug: exception should be handled in the scheduler
     * because otherwise there will b no progress made, if there is
     * no task at current position in time
     * */

    public void nextStep() throws RunOutOfTasksException {

        // currently, when working with tick handler,
        // the system will only know about the tasks
        // at the end of the first invocation of this method todo is it still valid?

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
