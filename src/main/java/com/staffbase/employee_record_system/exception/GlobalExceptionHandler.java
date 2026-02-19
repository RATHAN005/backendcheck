package com.staffbase.employee_record_system.exception;

import com.staffbase.employee_record_system.dto.APIResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<APIResponse<Map<String, String>>> handleValidationExceptions(
                        MethodArgumentNotValidException ex) {
                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getAllErrors().forEach((error) -> {
                        String fieldName = ((FieldError) error).getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.put(fieldName, errorMessage);
                });

                return new ResponseEntity<>(APIResponse.<Map<String, String>>builder()
                                .success(false)
                                .message("Validation failed")
                                .data(errors)
                                .status(HttpStatus.BAD_REQUEST.value())
                                .timestamp(LocalDateTime.now())
                                .build(), HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<APIResponse<String>> handleRuntimeExceptions(RuntimeException ex) {
                return new ResponseEntity<>(APIResponse.<String>builder()
                                .success(false)
                                .message(ex.getMessage())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .timestamp(LocalDateTime.now())
                                .build(), HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<APIResponse<String>> handleGeneralExceptions(Exception ex) {
                return new ResponseEntity<>(APIResponse.<String>builder()
                                .success(false)
                                .message("An unexpected error occurred: " + ex.getMessage())
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .timestamp(LocalDateTime.now())
                                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
}



