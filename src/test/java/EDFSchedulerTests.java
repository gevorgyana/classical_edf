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