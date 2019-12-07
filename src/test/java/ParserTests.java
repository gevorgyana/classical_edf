import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The hardcoded example in this fixture is a simple
 * example that works, for testing with random data,
 * see UsageExamples fixture
 *
 * todo test TimedTask parsing
 * */

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
    public void positiveTestCases() {
        assertAll(() -> {
            Config2TaskConverter converter = new Config2TaskConverter(idManager);

            // the name should be 'tasks.xml'
            ArrayList<Task> tasks = converter.parseTasks("tasks.xml");

            /**
             * The following code shows that the expected tasks match the parsed tasks
             * */

            assertEquals(tasks.size(), test1ExpectedTasks.size());

            for (int i = 0; i < tasks.size(); ++i) {

                Task task = tasks.get(i),
                        expectedTask = test1ExpectedTasks.get(i);

                assertEquals(task.getAbsoluteDeadline(), expectedTask.getAbsoluteDeadline());
                assertEquals(task.getWorstCaseRunningTime(), expectedTask.getWorstCaseRunningTime());
            }
        });
    }

    /**
     * This test shows what not to do with the parser, otherwise it will fail
     * */

    @Test
    public void negativeTestCases() {

    }
}
