package com.ridelink.ride.exception;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.ridelink.ride.dto.ErrorResponse;

// Centralized exception handler mapping domain and system exceptions to standardized HTTP responses.
@RestControllerAdvice
public class GlobalExceptionHandler {

	// Handles missing entities (e.g., ride not found), returning HTTP 404.
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, WebRequest request) {
		return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
	}

	// Handles invalid status transitions and driver availability conflicts, returning HTTP 409.
	@ExceptionHandler(InvalidRideStateException.class)
	public ResponseEntity<ErrorResponse> handleInvalidState(InvalidRideStateException ex, WebRequest request) {
		return build(HttpStatus.CONFLICT, ex.getMessage(), request, null);
	}

	@ExceptionHandler(NoAvailableDriverException.class)
	public ResponseEntity<ErrorResponse> handleNoDriver(NoAvailableDriverException ex, WebRequest request) {
		return build(HttpStatus.CONFLICT, ex.getMessage(), request, null);
	}

	// Handles failures during remote microservice communication, returning HTTP 502.
	@ExceptionHandler(ExternalServiceException.class)
	public ResponseEntity<ErrorResponse> handleExternal(ExternalServiceException ex, WebRequest request) {
		return build(HttpStatus.BAD_GATEWAY, ex.getMessage(), request, null);
	}

	// Handles bean validation failures (@Valid annotations), extracting field-specific errors into HTTP 400.
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
		List<String> details = ex.getBindingResult().getFieldErrors().stream()
				.map(err -> err.getField() + ": " + err.getDefaultMessage())
				.collect(Collectors.toList());
		return build(HttpStatus.BAD_REQUEST, "Validation failed", request, details);
	}

	// Handles illegal arguments and invalid method inputs, returning HTTP 400.
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArg(IllegalArgumentException ex, WebRequest request) {
		return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
	}

	// Catch-all fallback handler for uncaught exceptions, returning HTTP 500.
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, WebRequest request) {
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request, null);
	}

	// Helper method to assemble the standardized ErrorResponse payload.
	private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, WebRequest request,
			List<String> details) {
		ErrorResponse body = ErrorResponse.builder()
				.timestamp(Instant.now())
				.status(status.value())
				.error(status.getReasonPhrase())
				.message(message)
				.path(request.getDescription(false).replace("uri=", ""))
				.details(details)
				.build();
		return ResponseEntity.status(status).body(body);
	}
}
