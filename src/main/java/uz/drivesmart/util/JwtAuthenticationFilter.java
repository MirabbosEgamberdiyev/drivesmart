package uz.drivesmart.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Authorization header yo'q yoki noto'g'ri formatda
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Token'ni olish va BARCHA bo'sh joylarni olib tashlash
            String jwt = authHeader.substring(7);
            jwt = jwt.replaceAll("\\s+", ""); // Barcha whitespace'larni o'chirish

            // Token bo'sh emasligini tekshirish
            if (jwt.isEmpty()) {
                log.warn("JWT token bo'sh");
                filterChain.doFilter(request, response);
                return;
            }

            log.debug("JWT token length: {}", jwt.length());

            final String userEmail = jwtUtil.extractUsername(jwt);

            // Foydalanuvchi topildi va authentication yo'q
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                if (jwtUtil.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Foydalanuvchi autentifikatsiya qilindi: {}", userEmail);
                }
            }
        } catch (Exception e) {
            log.error("JWT autentifikatsiya xatoligi: {}", e.getMessage());
            // Xatolik bo'lsa ham filter chain davom etadi
        }

        filterChain.doFilter(request, response);
    }
}
