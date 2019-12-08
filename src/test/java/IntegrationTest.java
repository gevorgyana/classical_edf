import Exceptions.RunOutOfTasksException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * todo currently it is not known when
 * the latest execution happen - already fixed
 *
 * - randomly generate execution plan
 * - read it from xml
 * - schedule until (?)
 * */

public class IntegrationTest {
    @Test
    public void main() {

        TaskIDManager taskIDManager = new TaskIDManager();
        Logger logger = Logger.getLogger("without handler");
        LazyTicker ticker = new LazyTicker();
        EDFPolicy scheduler = new EDFPolicy(ticker, logger);

        ArrayList<TimedTask> tasks;
        ConfigParser parser = new ConfigParser(taskIDManager);
        tasks = parser.parseTimedTasks("config.xml");

        TickHandler handler = new TickHandler(scheduler, tasks);
        ticker.setHandler(handler);

        while (true) {
            try {
                scheduler.nextStep();
            } catch (RunOutOfTasksException e) {

            }
        }

    }
}
