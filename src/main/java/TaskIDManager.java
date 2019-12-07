public class TaskIDManager {

    private int currentID = 0;

    public int nextID() {
        ++currentID;
        return currentID;
    }
}
