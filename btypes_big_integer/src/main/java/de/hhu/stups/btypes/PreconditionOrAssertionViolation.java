package de.hhu.stups.btypes;

public class PreconditionOrAssertionViolation extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String message;

    public PreconditionOrAssertionViolation(String message) {
        super();
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
