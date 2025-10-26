package uz.drivesmart.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.drivesmart.dto.request.RegisterRequest;
import uz.drivesmart.dto.response.AuthResponse;
import uz.drivesmart.service.AuthService;

@Tag(name = "Auth", description = "Foydalanuvchi autentifikatsiyasi API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Foydalanuvchi ro‘yxatdan o‘tishi", description = "Yangi foydalanuvchi yaratadi va JWT token qaytaradi")
    @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli ro‘yxatdan o‘tildi")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Foydalanuvchi tizimga kirishi", description = "Foydalanuvchi email va parol bilan kiradi, JWT token qaytaradi")
    @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli kirildi")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}