package server.exceptions;

public class ResponseFailedException extends RuntimeException {
    public ResponseFailedException(String message) {
        super(message);
    }
}
