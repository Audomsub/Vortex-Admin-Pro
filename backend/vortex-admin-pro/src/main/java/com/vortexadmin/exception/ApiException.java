package com.vortexadmin.exception;

import org.springframework.http.HttpStatus;

/**
 * Application-level runtime exception used throughout Vortex Admin Pro to signal
 * business logic errors that should be translated directly into a specific HTTP
 * error response.
 *
 * <p>By carrying an {@link HttpStatus}, this exception bridges the gap between the
 * service layer (which should not depend on HTTP concepts) and the global exception
 * handler ({@link GlobalExceptionHandler}), which maps it to a structured JSON error
 * response with the appropriate HTTP status code.
 *
 * <p>Common usage patterns:
 * <pre>
 *   throw new ApiException(HttpStatus.NOT_FOUND, "User not found");
 *   throw new ApiException(HttpStatus.FORBIDDEN, "Insufficient permissions");
 *   throw new ApiException(HttpStatus.CONFLICT, "Username already taken");
 * </pre>
 *
 * <p>Extends {@link RuntimeException} so that callers are not required to declare
 * or catch it — Spring's {@code @ExceptionHandler} infrastructure in
 * {@link GlobalExceptionHandler} intercepts it automatically.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    /**
     * Constructs a new {@code ApiException} with the given HTTP status and detail message.
     *
     * @param status  the HTTP status code that the response should carry (e.g.,
     *                {@link HttpStatus#NOT_FOUND}, {@link HttpStatus#FORBIDDEN}).
     * @param message a human-readable description of the error, returned to the API
     *                client in the {@code "message"} field of the standard error response.
     */
    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    /**
     * Returns the HTTP status code associated with this exception.
     *
     * <p>Used by {@link GlobalExceptionHandler#handleApiException(ApiException)} to set
     * the response status when converting this exception into a JSON error response.
     *
     * @return the {@link HttpStatus} that this exception maps to.
     */
    public HttpStatus getStatus() {
        return status;
    }
}
