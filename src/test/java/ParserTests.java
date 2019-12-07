import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.HashMap;

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
           1, 1, idManager
        ));
    }

    @Test
    public void test1() {

        {
            Config2TaskConverter converter = new Config2TaskConverter(idManager);
            ArrayList<Task> tasks = converter.parseTasks("config");

            assertEquals(tasks.size(), test1ExpectedTasks.size());

            for (int i = 0; i < tasks.size(); ++i) {
                Task task = tasks.get(i),
                        expectedTask = test1ExpectedTasks.get(i);

                assertEquals(task.getAbsoluteDeadline(), expectedTask.getAbsoluteDeadline());
                assertEquals(task.getWorstCaseRunningTime(), expectedTask.getWorstCaseRunningTime());
            }
        };
    }
}
