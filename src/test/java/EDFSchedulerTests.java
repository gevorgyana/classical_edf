import Exceptions.RunOutOfTasksException;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

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
        ticker = new LazyTicker(handler);
    }

    @Test
    public void withoutTickHandler() {

        // todo check that the log is correct - seems good

        setUpCommon();

        ArrayList<Task> tasks = new ArrayList<Task>();
        tasks.add(new Task(2, 1, taskIDManager));
        tasks.add(new Task(5, 2, taskIDManager));
        tasks.add(new Task(10, 2, taskIDManager));
        tasks.add(new Task(9, 2, taskIDManager));

        assertAll(() -> {
            scheduler.acceptTasks(tasks);
        });

        while (true) {
            try {
                 scheduler.nextStep();
            } catch (RunOutOfTasksException e) {
                break;
            }
        }
    }

    @Test
    public void emptyTasksBuffer() {

        setUpCommon();

        assertThrows(RunOutOfTasksException.class, () -> {
            scheduler.nextStep();
        });
    }

    /**
     * it is not possible to use spies even with powermock,
     * because I am using junit 5, therefore I do not
     * show how the internals of the scheduler policy work
     * */

    @Test
    public void withTickHandlerCornerCases() {

        setUpCommon();

        ArrayList<TimedTask> tasks = new ArrayList<>();

        // pay attention to arrival time - here it is 2,
        // therefore if an exception occures, it means, that at THIS POINT
        // there are no tasks, -> the trick with [while(true), break if exception]
        // does not work anymore; u may need to relaunch the scheduler or ignore
        // the exceptions and wait for some period of time; it is better to
        // know how much time the simulation takes in advance and
        // do precisely so many iterations and then exit

        tasks.add(new TimedTask(
                new Task(1, 2, taskIDManager),
                2
        ));

        setUpWithTickHandler(tasks);

        /**
         * at this point, on every tick, tasks will be added
         * automatically to the scheduler
         * */

        int beenExecutedCounter = 0;

        while (true) {
            try {
                // increase counter before exception occurs
                ++beenExecutedCounter;
                scheduler.nextStep();
            } catch (RunOutOfTasksException e) {
                break;
            }
        }

        // we can never get to the first task with the loop above;
        assertEquals(1, beenExecutedCounter);
    }

    /**
     * It is better to know in advance how much time tasks
     * will take, if you are doing a simmulation, or keep
     * the scheduler alive all the time
     * */

    @Test
    public void withTickHandlerIntendedUse() {

        setUpCommon();

        ArrayList<TimedTask> tasks = new ArrayList<>();

        tasks.add(
                new TimedTask(
                        new Task(1, 1, taskIDManager),
                        2
                )
        );

        tasks.add(new TimedTask(
                new Task(1, 2, taskIDManager),
                2
        ));

        setUpWithTickHandler(tasks);
    }

    // todo random generation of execution plans

    @Test // todo move to another test
    public void integrationWithParser() {

    }
}
