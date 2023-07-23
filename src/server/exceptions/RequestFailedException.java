package server.exceptions;

public class RequestFailedException extends RuntimeException {
    public RequestFailedException(String message) {
        super(message);
    }
}
