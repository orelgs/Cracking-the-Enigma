package exceptions.machine;

public class MachineNotLoadedException extends Exception {
    private final String EXCEPTION_MESSAGE = "Machine file has not been loaded!";

    @Override
    public String getMessage() {
        return EXCEPTION_MESSAGE;
    }
}