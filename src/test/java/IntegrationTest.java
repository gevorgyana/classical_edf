import Exceptions.RunOutOfTasksException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.logging.Logger;

public class IntegrationTest {
    @Test
    public void main() {

        TaskIDManager taskIDManager = new TaskIDManager();
        Logger logger = Logger.getLogger("wit handler");
        LazyTicker ticker = new LazyTicker();
        EDFPolicy scheduler = new EDFPolicy(ticker, logger);

        ArrayList<TimedTask> tasks;
        ConfigParser parser = new ConfigParser(taskIDManager);
        tasks = parser.parseTimedTasks("config.xml");

        TickHandler handler = new TickHandler(scheduler, tasks);
        ticker.setHandler(handler);

        for (int i = 0; i < scheduler.getEndOfExecutionHint(); ++i) {
            scheduler.nextStep();
        }

    }
}
