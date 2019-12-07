package Exceptions;

public class SchedulabilityViolationException extends Exception {

    public SchedulabilityViolationException() {}

    public SchedulabilityViolationException(String gripe) {
        super(gripe);
    }
}
