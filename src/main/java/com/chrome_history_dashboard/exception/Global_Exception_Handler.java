package com.chrome_history_dashboard.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class Global_Exception_Handler {

	@ExceptionHandler(Chrome_History_Sync_Exception.class)
    public ResponseEntity<Map<String, String>> handleSyncError(Chrome_History_Sync_Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
    }
	
}
