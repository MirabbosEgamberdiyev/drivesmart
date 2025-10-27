package uz.drivesmart.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import uz.drivesmart.dto.response.ApiResponseDto;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * JWT authentication entry point - authentication xatolari uchun
 * Foydalanuvchi authenticate bo'lmagan holda protected resource'ga murojaat qilganda ishga tushadi
 */
@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Authentication xatolari uchun commence method
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param authException Authentication exception
     * @throws IOException IO exception
     * @throws ServletException Servlet exception
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.AuthenticationException authException) throws IOException, ServletException {
        log.error("Responding with unauthorized error. Message - {}", authException.getMessage());

        // Response headers o'rnatish
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");

        // Error response yaratish
        ApiResponseDto<Object> errorResponse = ApiResponseDto.builder()
                .success(false)
                .message("Avtorizatsiya talab etiladi")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        // JSON response yozish
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}