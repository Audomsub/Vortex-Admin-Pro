package com.vortexadmin.exception;

import com.vortexadmin.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralised exception handler for the Vortex Admin Pro REST API.
 *
 * <p>Annotated with {@code @RestControllerAdvice}, this class intercepts exceptions
 * thrown by any {@code @RestController} in the application and converts them into
 * consistent, structured JSON error responses conforming to the standard API response
 * format:
 * <pre>
 *   {
 *     "success": false,
 *     "message": "...",
 *     "errors": { ... }   // optional, only for validation failures
 *   }
 * </pre>
 *
 * <p>Exception tiers handled (most-specific first):
 * <ol>
 *   <li>{@link ApiException} – business logic errors with a caller-defined HTTP status.</li>
 *   <li>{@link MethodArgumentNotValidException} – Bean Validation failures → 400.</li>
 *   <li>{@link AccessDeniedException} – insufficient authority → 403.</li>
 *   <li>{@link NoResourceFoundException} – no route matched → 404.</li>
 *   <li>{@link HttpRequestMethodNotSupportedException} – wrong HTTP verb → 405.</li>
 *   <li>{@link MethodArgumentTypeMismatchException} – path variable type error → 400.</li>
 *   <li>{@link Exception} – catch-all for unexpected errors → 500 (stack trace logged).</li>
 * </ol>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles {@link ApiException} instances thrown by the service layer.
     *
     * <p>Returns the exception's message directly to the client, paired with the HTTP
     * status code that the exception carries. No stack trace or internal details are
     * exposed.
     *
     * @param ex the {@link ApiException} thrown by a service or controller.
     * @return a {@link ResponseEntity} wrapping an {@link ApiResponse} error body with
     *         the exception's HTTP status.
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex) {
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), ex.getStatus());
    }

    /**
     * Handles Bean Validation failures triggered when a request body fails
     * {@code @Valid} or {@code @Validated} constraints.
     *
     * <p>Collects all field-level validation errors from the binding result and
     * returns them as a {@code Map<String, String>} where each key is the field name
     * and the value is the first constraint violation message. The HTTP status is always
     * {@code 400 Bad Request}.
     *
     * @param ex the {@link MethodArgumentNotValidException} produced by Spring MVC
     *           when request body validation fails.
     * @return a {@link ResponseEntity} with HTTP 400 and a map of field-level errors
     *         embedded in the standard API response structure.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(ApiResponse.error("Validation failed", errors), HttpStatus.BAD_REQUEST);
    }

    /**
     * Catch-all handler for any {@link Exception} not matched by more specific handlers.
     *
     * <p>Logs the full exception (including stack trace) at ERROR level for server-side
     * diagnosis, but returns only a generic, user-safe message to the client to avoid
     * leaking internal implementation details. The HTTP status is always
     * {@code 500 Internal Server Error}.
     *
     * @param ex the unhandled {@link Exception}.
     * @return a {@link ResponseEntity} with HTTP 500 and a generic error message.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        return new ResponseEntity<>(ApiResponse.error("Access denied: insufficient permissions"), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(NoResourceFoundException ex) {
        return new ResponseEntity<>(ApiResponse.error("Resource not found: " + ex.getResourcePath()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return new ResponseEntity<>(ApiResponse.error("Method not allowed: " + ex.getMethod()), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return new ResponseEntity<>(ApiResponse.error("Invalid value for parameter '" + ex.getName() + "': " + ex.getValue()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex) {
        logger.error("Unhandled exception: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(ApiResponse.error("An unexpected error occurred. Please try again later."), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
