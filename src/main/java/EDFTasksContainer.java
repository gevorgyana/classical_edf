import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * Delegates requests to a heap
 * */

public class EDFTasksContainer implements Iterable<Task> {

    private PriorityQueue<Task> containerImplementation;

    EDFTasksContainer() {
        containerImplementation = new PriorityQueue<>(new Comparator<Task>() {
            @Override
            public int compare(Task task, Task t1) {
                return task.getAbsoluteDeadline() - t1.getAbsoluteDeadline();
            }
        });
    }

    public boolean isEmpty() {
        return containerImplementation.isEmpty();
    }

    public Task getNextTask() {
        return containerImplementation.remove();
    }

    public boolean add(Task task) {
        return containerImplementation.add(task);
    }

    @Override
    public Iterator<Task> iterator() {
        return containerImplementation.iterator();
    }
}
