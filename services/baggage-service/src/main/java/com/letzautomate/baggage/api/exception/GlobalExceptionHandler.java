package com.letzautomate.baggage.api.exception;

import com.letzautomate.baggage.api.dto.ErrorResponse;
import com.letzautomate.baggage.application.BaggageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(BaggageService.BagNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleBagNotFound(BaggageService.BagNotFoundException ex) {
		log.warn("Bag not found: {}", ex.getMessage());
		
		// Extract bagTag from message if present
		String message = ex.getMessage();
		String bagTag = null;
		if (message.contains(":")) {
			bagTag = message.substring(message.indexOf(":") + 1).trim();
		}
		
		ErrorResponse error = new ErrorResponse("BAG_NOT_FOUND", ex.getMessage(), bagTag);
		return ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.contentType(MediaType.APPLICATION_XML)
				.body(error);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(FieldError::getDefaultMessage)
				.collect(Collectors.joining("; "));
		
		log.warn("Validation error: {}", message);
		
		ErrorResponse error = new ErrorResponse("SCHEMA_VALIDATION_FAILED", message);
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.contentType(MediaType.APPLICATION_XML)
				.body(error);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex) {
		String message = "Invalid XML format or structure";
		if (ex.getCause() != null && ex.getCause().getMessage() != null) {
			message = ex.getCause().getMessage();
		}
		
		log.warn("XML parsing error: {}", message);
		
		ErrorResponse error = new ErrorResponse("SCHEMA_VALIDATION_FAILED", message);
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.contentType(MediaType.APPLICATION_XML)
				.body(error);
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
		String message = "Unsupported media type. Only application/xml is supported";
		log.warn("Unsupported media type: {}", ex.getContentType());
		
		ErrorResponse error = new ErrorResponse("UNSUPPORTED_MEDIA_TYPE", message);
		return ResponseEntity
				.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
				.contentType(MediaType.APPLICATION_XML)
				.body(error);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericError(Exception ex) {
		log.error("Unexpected error", ex);
		
		ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred");
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.contentType(MediaType.APPLICATION_XML)
				.body(error);
	}
}
