import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ParserTests {

    private TaskIDManager idManager;
    private ArrayList<Task> test1ExpectedTasks;

    @BeforeAll
    private void setUp() {
        idManager = new TaskIDManager();
        test1ExpectedTasks = new ArrayList<>();

        test1ExpectedTasks.add(new Task(
           2, 1, idManager
        ));
        test1ExpectedTasks.add(new Task(
            5, 2, idManager
        ));
        test1ExpectedTasks.add(new Task(
                4, 2, idManager
        ));
        test1ExpectedTasks.add(new Task(
                10, 2, idManager
        ));
        test1ExpectedTasks.add(new Task(
                9, 2, idManager
        ));
    }

    /**
     * To work with parser, copy code from here
     * */

    @Test
    public void usageExample() {
        assertAll(() -> {
            ConfigParser converter = new ConfigParser(idManager);

            ArrayList<TimedTask> tasks = converter.parseTimedTasks("config.xml");

            /**
             * The following code shows that the expected tasks match the parsed tasks
             * */

            {
                assertEquals(tasks.size(), test1ExpectedTasks.size());
                for (int i = 0; i < tasks.size(); ++i) {

                    TimedTask task = tasks.get(i);
                    Task expectedTask = test1ExpectedTasks.get(i);

                    assertEquals(task.getAbsoluteDeadline(), expectedTask.getAbsoluteDeadline());
                    assertEquals(task.getWorstCaseRuntime(), expectedTask.getWorstCaseRunningTime());
                }
            }
        });
    }
}
