package uz.drivesmart.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import uz.drivesmart.dto.response.ApiResponseDto;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * JWT access denied handler - ruxsat yo'qligi uchun
 * Foydalanuvchi authenticate bo'lgan lekin yetarli ruxsati yo'q bo'lganda ishga tushadi
 */
@Component
@Slf4j
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public JwtAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Access denied xatolari uchun handle method
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param accessDeniedException Access denied exception
     * @throws IOException IO exception
     * @throws ServletException Servlet exception
     */
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        log.error("Responding with access denied error. Message - {}", accessDeniedException.getMessage());

        // Response headers o'rnatish
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding("UTF-8");

        // Error response yaratish
        ApiResponseDto<Object> errorResponse = ApiResponseDto.builder()
                .success(false)
                .message("Sizda bu amalni bajarish uchun yetarli ruxsat yo'q")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        // JSON response yozish
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}