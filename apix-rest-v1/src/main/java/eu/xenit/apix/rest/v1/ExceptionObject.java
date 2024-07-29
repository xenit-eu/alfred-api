package eu.xenit.apix.rest.v1;

public class ExceptionObject {

    private final String message;

    public ExceptionObject(Error error) {
        this.message = error.getMessage();
    }

    public String getMessage() {
        return this.message;
    }
}
