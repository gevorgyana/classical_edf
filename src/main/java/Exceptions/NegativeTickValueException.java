package Exceptions;

public class NegativeTickValueException extends RuntimeException {
    public NegativeTickValueException() {}
    public NegativeTickValueException(String gripe) {
        super(gripe);
    }
}
