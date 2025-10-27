package esi.ma.backend.common.exception;


import esi.ma.backend.common.dto.ErrorDetails;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle resource not found exceptions
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFound(
            ResourceNotFoundException ex
    ) {
        log.warn("Resource not found: {}", ex.getMessage());

        ErrorDetails error = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handle weather API exceptions
     */
    @ExceptionHandler(WeatherApiException.class)
    public ResponseEntity<ErrorDetails> handleWeatherApiException(
            WeatherApiException ex
    ) {
        log.error("Weather API error: {}", ex.getMessage(), ex);

        ErrorDetails error = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error("Service Unavailable")
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    /**
     * Handle ML service exceptions
     */
    @ExceptionHandler(MLServiceException.class)
    public ResponseEntity<ErrorDetails> handleMLServiceException(
            MLServiceException ex
    ) {
        log.error("ML service error: {}", ex.getMessage(), ex);

        ErrorDetails error = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error("ML Service Unavailable")
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    /**
     * Handle invalid request exceptions
     */
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorDetails> handleInvalidRequest(
            InvalidRequestException ex
    ) {
        log.warn("Invalid request: {}", ex.getMessage());

        ErrorDetails error = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handle rate limit exceptions
     */
    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ErrorDetails> handleRateLimit(
            RateLimitException ex
    ) {
        log.warn("Rate limit exceeded: {}", ex.getMessage());

        ErrorDetails error = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .error("Too Many Requests")
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }

    /**
     * Handle invalid file exceptions
     */
    @ExceptionHandler(InvalidRequestException.InvalidFileException.class)
    public ResponseEntity<ErrorDetails> handleInvalidFile(
            InvalidRequestException.InvalidFileException ex
    ) {
        log.warn("Invalid file: {}", ex.getMessage());

        ErrorDetails error = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid File")
                .message(ex.getMessage())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidationErrors(
            MethodArgumentNotValidException ex
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorDetails error = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Input validation failed")
                .validationErrors(errors)
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handle constraint violations
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDetails> handleConstraintViolation(
            ConstraintViolationException ex
    ) {
        // For Hibernate ConstraintViolationException, we need to handle differently
        // This exception has different methods than javax validation constraint violations
        Map<String, String> errors = new HashMap<>();
        errors.put("database", ex.getMessage());

        ErrorDetails error = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Constraint Violation")
                .message("Validation constraints violated")
                .validationErrors(errors)
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handle type mismatch errors
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDetails> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex
    ) {
        String message = String.format(
                "Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(),
                ex.getName(),
                ex.getRequiredType().getSimpleName()
        );

        ErrorDetails error = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Type Mismatch")
                .message(message)
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);

        ErrorDetails error = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred. Please try again later.")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}