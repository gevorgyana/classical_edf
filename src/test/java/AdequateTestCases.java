import Exceptions.RunOutOfTasksException;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AdequateTestCases {

    private TaskIDManager taskIDManager;
    private Logger logger;
    private LazyTicker ticker;
    private EDFPolicy scheduler;

    @BeforeAll
    private void setUp() {
        taskIDManager = new TaskIDManager();
        logger = Logger.getLogger("");
        ticker = new LazyTicker();
        scheduler = new EDFPolicy(ticker, logger);
    }

    @Test
    public void foo() {
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
        assertThrows(RunOutOfTasksException.class, () -> {
            scheduler.nextStep();
        });
    }

    /**
     * Implement reading from a file - config!
     * */
}
