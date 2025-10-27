package uz.drivesmart.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import uz.drivesmart.dto.response.ApiResponseDto;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler - barcha exception'larni bir joyda boshqaradi
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Resource not found exception handler
     * Resurs topilmasa ishga tushadi
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());

        ApiResponseDto<Object> errorResponse = ApiResponseDto.builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Business exception handler
     * Business logic xatoliklari uchun
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleBusinessException(
            BusinessException ex, WebRequest request) {
        log.warn("Business exception occurred: {}", ex.getMessage());

        ApiResponseDto<Object> errorResponse = ApiResponseDto.builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Validation exception handler
     * @Valid, @Validated annotationlari bilan validation xatoliklari
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Validation error occurred");

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });

        ApiResponseDto<Map<String, String>> errorResponse = ApiResponseDto.<Map<String, String>>builder()
                .success(false)
                .message("Validation xatolari")
                .data(errors)
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * JWT token xatoliklari - noto'g'ri format yoki signature
     */
    @ExceptionHandler({
            MalformedJwtException.class,
            SignatureException.class
    })
    public ResponseEntity<ApiResponseDto<Object>> handleJwtErrors(
            Exception ex, WebRequest request) {
        log.error("JWT error: {}", ex.getMessage());

        ApiResponseDto<Object> errorResponse = ApiResponseDto.builder()
                .success(false)
                .message("Token noto'g'ri formatda yoki yaroqsiz")
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * JWT token muddati tugagan
     */
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleExpiredJwt(
            ExpiredJwtException ex, WebRequest request) {
        log.error("JWT expired: {}", ex.getMessage());

        ApiResponseDto<Object> errorResponse = ApiResponseDto.builder()
                .success(false)
                .message("Token muddati tugagan")
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Authentication xatoliklari
     * Login/password noto'g'ri bo'lsa
     */
    @ExceptionHandler({
            BadCredentialsException.class,
            UsernameNotFoundException.class,
            AuthenticationException.class
    })
    public ResponseEntity<ApiResponseDto<Object>> handleAuthenticationException(
            Exception ex, WebRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());

        ApiResponseDto<Object> errorResponse = ApiResponseDto.builder()
                .success(false)
                .message("Telefon raqam yoki parol noto'g'ri")
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Access denied exception handler
     * Foydalanuvchi ruxsatsiz resource'ga kirishga urinsa
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        log.warn("Access denied: {}", ex.getMessage());

        ApiResponseDto<Object> errorResponse = ApiResponseDto.builder()
                .success(false)
                .message("Sizda bu amalni bajarish uchun ruxsat yo'q")
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Database constraint violation handler
     * Unique, Foreign key va boshqa DB constraint xatoliklari
     */
    @ExceptionHandler({
            DataIntegrityViolationException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<ApiResponseDto<Object>> handleDatabaseException(
            Exception ex, WebRequest request) {
        log.error("Database constraint violation: {}", ex.getMessage());

        String message = "Ma'lumotlar bazasi xatoligi yuz berdi";

        // Unique constraint xatoligi
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("Duplicate entry") ||
                    ex.getMessage().contains("duplicate key")) {
                message = "Bu ma'lumot allaqachon mavjud";
            } else if (ex.getMessage().contains("foreign key")) {
                message = "Bog'langan ma'lumot mavjud, o'chirib bo'lmaydi";
            }
        }

        ApiResponseDto<Object> errorResponse = ApiResponseDto.builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Illegal argument exception handler
     * Noto'g'ri parametrlar yuborilsa
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {
        log.error("Illegal argument: {}", ex.getMessage());

        ApiResponseDto<Object> errorResponse = ApiResponseDto.builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Media type not acceptable handler
     * Accept header noto'g'ri bo'lsa
     */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleMediaTypeNotAcceptable(
            HttpMediaTypeNotAcceptableException ex, WebRequest request) {
        log.error("Media type not acceptable: {}", ex.getMessage());

        ApiResponseDto<Object> errorResponse = ApiResponseDto.builder()
                .success(false)
                .message("So'rov qilingan media type qabul qilinmaydi")
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(errorResponse);
    }

    /**
     * Generic exception handler - eng oxirgi handler
     * Boshqa handler'lar tutmagan barcha exception'lar uchun
     * DIQQAT: Bu handler eng oxirida bo'lishi kerak!
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Object>> handleGenericException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error occurred", ex);

        ApiResponseDto<Object> errorResponse = ApiResponseDto.builder()
                .success(false)
                .message("Ichki server xatoligi")
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Request path ni olish uchun helper method
     */
    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}