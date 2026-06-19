package com.beepollen.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Global exception handler that provides unified error handling for both
 * REST API endpoints (JSON responses) and Thymeleaf web views (error pages).
 *
 * <p>API requests (URIs starting with {@code /api/}) receive structured JSON error bodies.
 * Web requests receive a rendered {@code error} Thymeleaf template with model attributes.</p>
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // -----------------------------------------------------------------------
    // ResourceNotFoundException -> 404
    // -----------------------------------------------------------------------

    /**
     * Handles {@link ResourceNotFoundException} — returns 404.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public Object handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());

        if (isApiRequest(request)) {
            Map<String, Object> body = buildErrorBody(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }

        return buildErrorView(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // DuplicateResourceException -> 409
    // -----------------------------------------------------------------------

    /**
     * Handles {@link DuplicateResourceException} — returns 409.
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public Object handleDuplicateResource(DuplicateResourceException ex, HttpServletRequest request) {
        log.warn("Duplicate resource: {}", ex.getMessage());

        if (isApiRequest(request)) {
            Map<String, Object> body = buildErrorBody(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }

        return buildErrorView(HttpStatus.CONFLICT, ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // MethodArgumentNotValidException -> 400
    // -----------------------------------------------------------------------

    /**
     * Handles bean-validation failures — returns 400 with per-field error details.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation failed: {}", ex.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        if (isApiRequest(request)) {
            Map<String, Object> body = buildErrorBody(HttpStatus.BAD_REQUEST, "Validation Failed",
                    "One or more fields have invalid values", request);
            body.put("fieldErrors", fieldErrors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }

        ModelAndView mav = new ModelAndView("error");
        mav.addObject("status", HttpStatus.BAD_REQUEST.value());
        mav.addObject("error", "Validation Failed");
        mav.addObject("message", "One or more fields have invalid values");
        mav.addObject("fieldErrors", fieldErrors);
        return mav;
    }

    // -----------------------------------------------------------------------
    // AccessDeniedException -> 403
    // -----------------------------------------------------------------------

    /**
     * Handles Spring Security {@link AccessDeniedException} — returns 403.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied: {}", ex.getMessage());

        if (isApiRequest(request)) {
            Map<String, Object> body = buildErrorBody(HttpStatus.FORBIDDEN, "Forbidden",
                    "You do not have permission to access this resource", request);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
        }

        return buildErrorView(HttpStatus.FORBIDDEN, "You do not have permission to access this resource");
    }

    // -----------------------------------------------------------------------
    // MaxUploadSizeExceededException -> 413
    // -----------------------------------------------------------------------

    /**
     * Handles file upload size limits — returns 413.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Object handleMaxSizeException(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        log.warn("File upload exceeded max size: {}", ex.getMessage());

        if (isApiRequest(request) || request.getRequestURI().startsWith("/ai-assistant")) {
            Map<String, Object> body = buildErrorBody(HttpStatus.PAYLOAD_TOO_LARGE, "Payload Too Large",
                    "File upload quá lớn! Vui lòng chọn file có kích thước dưới 5MB.", request);
            body.put("error", "File upload quá lớn! Vui lòng chọn file có kích thước dưới 5MB.");
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(body);
        }

        return buildErrorView(HttpStatus.PAYLOAD_TOO_LARGE, "File upload quá lớn! Vui lòng chọn file có kích thước dưới 5MB.");
    }

    // -----------------------------------------------------------------------
    // DataIntegrityViolationException -> 400
    // -----------------------------------------------------------------------

    /**
     * Handles database constraint violations (e.g. trying to delete an entity
     * that is referenced by foreign keys) — returns 400.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Object handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Data integrity violation: {}", ex.getMessage());

        String message = "Không thể thực hiện thao tác này vì dữ liệu đang được liên kết ở một nơi khác (ví dụ: đang có lịch sử thu hoạch tham chiếu tới). Hãy xoá các dữ liệu liên quan trước.";
        if (isApiRequest(request)) {
            Map<String, Object> body = buildErrorBody(HttpStatus.BAD_REQUEST, "Data Integrity Violation",
                    message, request);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }

        return buildErrorView(HttpStatus.BAD_REQUEST, message);
    }

    // -----------------------------------------------------------------------
    // General Exception -> 500
    // -----------------------------------------------------------------------

    /**
     * Catch-all handler for unexpected exceptions — returns 500.
     */
    @ExceptionHandler(Exception.class)
    public Object handleGeneralException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error occurred", ex);
        try {
            File f = new File("error_trace.txt");
            try (PrintWriter pw = new PrintWriter(new FileWriter(f, true))) {
                pw.println("----- " + LocalDateTime.now() + " -----");
                ex.printStackTrace(pw);
                pw.println();
            }
        } catch (IOException e) {
            log.error("Could not write error trace", e);
        }

        if (isApiRequest(request)) {
            Map<String, Object> body = buildErrorBody(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                    "An unexpected error occurred. Please try again later.", request);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }

        return buildErrorView(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
    }

    // -----------------------------------------------------------------------
    // Helper methods
    // -----------------------------------------------------------------------

    /**
     * Determines whether the incoming request is an API call (JSON) or a web
     * (Thymeleaf) request by inspecting the URI prefix.
     */
    private boolean isApiRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/");
    }

    /**
     * Builds a structured error body map for API responses.
     */
    private Map<String, Object> buildErrorBody(HttpStatus status, String error, String message,
                                                HttpServletRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("path", request.getRequestURI());
        return body;
    }

    /**
     * Builds a {@link ModelAndView} targeting the Thymeleaf {@code error} template.
     */
    private ModelAndView buildErrorView(HttpStatus status, String message) {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("status", status.value());
        mav.addObject("error", status.getReasonPhrase());
        mav.addObject("message", message);
        mav.addObject("timestamp", LocalDateTime.now());
        return mav;
    }
}
