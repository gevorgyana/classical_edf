import Exceptions.RunOutOfTasksException;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EDFSchedulerTests {

    private TaskIDManager taskIDManager;
    private Logger logger;
    private LazyTicker ticker;
    private TickHandler handler = null;
    private EDFPolicy scheduler;

    private void setUpCommon() {
        taskIDManager = new TaskIDManager();
        logger = Logger.getLogger("without handler");
        ticker = new LazyTicker();
        scheduler = new EDFPolicy(ticker, logger);
    }

    private void setUpWithTickHandler(ArrayList<TimedTask> tasks) {
        handler = new TickHandler(scheduler, tasks);
        ticker.setHandler(handler);
    }

    @Test
    public void withoutTickHandler() {

        setUpCommon();

        ArrayList<Task> tasks = new ArrayList<Task>();
        tasks.add(new Task(2, 1, taskIDManager));
        tasks.add(new Task(5, 2, taskIDManager));
        tasks.add(new Task(10, 2, taskIDManager));
        tasks.add(new Task(9, 2, taskIDManager));

        assertAll(() -> {
            scheduler.acceptTasks(tasks);
        });

        for (int i = 0; i < 5; ++i) {
            scheduler.nextStep();
        }
    }

    @Test
    public void emptyTasksBuffer() {

        setUpCommon();

        /**
         * this method cannot throw;
         * explanation: the scheduler cannot decide
         * when it finished, it may never finish, because
         * at any instant, there may be new tasks
         * */

        assertAll(() -> {
            scheduler.nextStep();
        });
    }

    @Test
    public void withTickHandlerCornerCases() {

        taskIDManager = new TaskIDManager();
        logger = Logger.getLogger("without handler");
        ticker = new LazyTicker();
        scheduler = new EDFPolicy(ticker, logger);
        EDFPolicy spyScheduler = spy(scheduler);

        ArrayList<TimedTask> tasks = new ArrayList<>();

        /**
         * pay attention to arrival time - here it is 2,
         * therefore if an exception occures, it means, that at THIS POINT
         * there are no tasks, -> the trick with [while(true), break if exception]
         * does not work anymore; u may need to relaunch the scheduler or ignore
         * the exceptions for some period of time; it is better to
         * know how much time the simulation takes in advance and
         * do precisely so many iterations and then exit
         * */

        tasks.add(new TimedTask(
                new Task(2, 1, taskIDManager),
                2
        ));

        handler = new TickHandler(spyScheduler, tasks);

        ticker.setHandler(handler);

        /**
         * at this point, on every tick, tasks will be added
         * automatically to the scheduler
         * */

        spyScheduler.nextStep();
        // second invocation makes this test pass
        spyScheduler.nextStep();

        /**
         * Task will be accepted only after tick 1 -
         * therefore even though we did 2 ticks, only 1 call
         * is registered!
         * */
        verify(spyScheduler, times(1)).acceptTasks((Task) any());
    }

    /**
     * It is better to know in advance how much time tasks
     * will take (if you are doing a simulation), or keep
     * the scheduler alive all the time; other words, it does
     * not make sense to demand from a scheduler being able to stop
     * as it does not know if there will be any task soon
     * */

    @Test
    public void withTickHandlerIntendedUse() {

        taskIDManager = new TaskIDManager();
        logger = Logger.getLogger("without handler");
        ticker = new LazyTicker();
        scheduler = new EDFPolicy(ticker, logger);
        EDFPolicy spyScheduler = spy(scheduler);

        ArrayList<TimedTask> tasks = new ArrayList<>();

        tasks.add(
                new TimedTask(
                        new Task(1, 1, taskIDManager),
                        2
                )
        );

        /**
         * todo show this
         * the scheduler will not accept this obviously
         *
         * tasks.add(new TimedTask(
         *                 new Task(1, 2, taskIDManager),
         *                 2
         *         ));
         * */

        // todo verify this with mocks
        // inside, the handler notifies the scheduler about the longest
        // task arrival time it has -> therefore in code, we can get the expected
        // simulation end from the scheduler and loop so many iterations and then exit
        handler = new TickHandler(spyScheduler, tasks);

        spyScheduler.nextStep();

        assertEquals(2, spyScheduler.getEndOfExecutionHint());
    }

    // todo random generation of execution plans
}

/**
 * Earliest deadline first policy
 *
 * Note! The SCHED_DEADLINE uses CBS algorithm,
 * which is a solution to the soft real time
 * scheduling problem; in this simulation, we
 * are working with hard real-time limitations, so
 * the fact that much of what is said in the documentation
 * about CBS is not used in this simulation should not be
 * confusing
 *
 * Limitations and applications:
 *
 * - no task is able to break its worst case
 * execution time guarantee; a task is required to
 * obey its worst case promise, and the system
 * makes sure that the tasks's deadline will be
 * respected;
 *
 * (it is possible to provide a mechanism to
 * in the EDFPolicy class to enforce deadline obedience even if
 * some process misbehaves (this is not real time anymore!),
 * but, generally speaking,
 * it will require other technique like CSB
 * (Constant Bandwidth Server) and is out of the scope of
 * this simulation)
 *
 * - currently, the notion of periodicity is not accepted;
 * a task can run in sporadic mode only
 * (once it executes, it disappears);
 *
 * TODO this simulation may be easily extended to
 * support periodic tasks; conceptually it is
 * similar to automated 'revival' of the task every PERIOD
 * number of steps, and in equivalent to just creating a new
 * task by hand, therefore it has nothing to do
 * with the algorithm (for description of periodic and
 * sporadic tasks, see documentation for deadline scheduler, section 3)
 *
 * - the tasks are not allowed to block in the middle of
 * their execution; this is called  Liu and Layland
 * model of task execution (for explanation of the difficulties the
 * self-blocking tasks introduce, see Constant Bandwidth Server Revisited,
 * for example, here http://ceur-ws.org/Vol-1291/ewili14_5.pdf), also here
 * is my own explanation why this case is difficult
 *
 * Classical EDF does not deal with blocking in the middle of their execution tasks;
 * and the Wikipedia is confusing by not providing a link to the original article
 * which explains that.
 *
 * The reason why the classical EDF cannot be applied (at least, without proper
 * modifications) is that according to the classical problem statement the system
 * is only restricted on being able to finish the task before a specific absolute point in time;
 * in case we want to allow the tasks to block at well-defined intervals, the restriction on the
 * scheduler becomes much more harsh.
 *
 * Here is an example that demonstrates that the problem is more complex if
 * we allow tasks to block at well-defined points in time (I do not even consider
 * the problem when a task is allowed to block during any time interval,
 * as it is becoming to make little sense to demand from a scheduler being able to guarantee
 * hard real-time behaviour in this case):
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
 * We cannot accept the tasks, as their execution times overlap and there is no
 * time available to execute both of them, while at point 3 we are making no progress
 * But the classical EDF would accept these tasks. A simple idea how to extend the classical
 * EDF check is by making sure that there is no time overlap, and even if there is, we are still
 * still have enough time to do the work before deadline. But this is a substantial modification of
 * the algorithm and is therefore not implemented here
 *
 * Also, there is a theoretical issue with this implementation, called
 * priority inversion (in short, it happens when a process with lower priority (earliest deadline
 * in our case) blocks on
 * some resource, and the task with higher priority is forced to wait until the task with lower priority finishes),
 * which can be solved with a technique called priority inheritance, it is not considered in code
 * */

