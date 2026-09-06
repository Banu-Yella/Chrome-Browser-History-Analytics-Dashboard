package com.chrome_history_dashboard.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.chrome_history_dashboard.browser_history_controller.Browser_History_Controller;
import com.chrome_history_dashboard.browser_history_tag_service.Browser_History_Tag_Service;
import com.chrome_history_dashboard.tag_service.Tag_Service;

@RestControllerAdvice
public class Global_Exception_Handler {

	@ExceptionHandler(Chrome_History_Sync_Exception.class)
	public ResponseEntity<Map<String, String>> handleSyncError(Chrome_History_Sync_Exception e) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
	}

	@ExceptionHandler({ Tag_Service.NoSuchTagException.class, Browser_History_Tag_Service.NoSuchTagLinkException.class,
			Browser_History_Controller.NoSuchHistoryException.class })
	public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
		String message = e.getBindingResult().getFieldErrors().stream()
				.map(f -> f.getField() + ": " + f.getDefaultMessage()).reduce((a, b) -> a + "; " + b)
				.orElse("Validation failed");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", message));
	}

}
