package cn.chahuyun.session.exception;

/** Normal termination of an abandoned interactive command. */
public class InteractionTimeoutException extends RuntimeException {
    public InteractionTimeoutException(String message) {
        super(message);
    }

    public InteractionTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
