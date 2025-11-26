package uz.drivesmart.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.drivesmart.dto.request.*;
import uz.drivesmart.dto.response.ApiResponseDto;
import uz.drivesmart.dto.response.AuthResponseDto;
import uz.drivesmart.dto.response.UserResponseDto;
import uz.drivesmart.dto.response.VerificationSentResponseDto;
import uz.drivesmart.security.UserPrincipal;
import uz.drivesmart.service.impl.AuthService;

/**
 * Mukammal Autentifikatsiya Controller
 * SMS/Email tasdiqlash, Refresh token, Password reset
 */
@Tag(name = "Authentication", description = "Foydalanuvchi autentifikatsiyasi va avtorizatsiyasi")
@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
@Validated
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ==================== REGISTRATION ====================

    @Operation(
            summary = "Ro'yxatdan o'tish boshlang'ich so'rov",
            description = "Tasdiqlash kodi yuboriladi (SMS yoki Email)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasdiqlash kodi yuborildi"),
            @ApiResponse(responseCode = "400", description = "Telefon/Email allaqachon mavjud"),
            @ApiResponse(responseCode = "422", description = "Validatsiya xatolari")
    })
    @PostMapping("/register/init")
    public ResponseEntity<ApiResponseDto<VerificationSentResponseDto>> initiateRegistration(
            @Valid @RequestBody RegisterInitRequestDto request) {
        log.info("Registration init request");

        VerificationSentResponseDto response = authService.initiateRegistration(request);

        return ResponseEntity.ok(ApiResponseDto.success("Tasdiqlash kodi yuborildi", response));
    }

    @Operation(
            summary = "Ro'yxatdan o'tishni tasdiqlash",
            description = "Tasdiqlash kodini tekshirib, ro'yxatdan o'tish jarayonini yakunlaydi"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ro'yxatdan o'tish muvaffaqiyatli"),
            @ApiResponse(responseCode = "400", description = "Tasdiqlash kodi noto'g'ri")
    })
    @PostMapping("/register/verify")
    public ResponseEntity<ApiResponseDto<AuthResponseDto>> completeRegistration(
            @Valid @RequestBody RegisterInitRequestDto request,
            @RequestParam String code) {
        log.info("Registration verification request");

        AuthResponseDto response = authService.completeRegistration(request, code);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Ro'yxatdan o'tish muvaffaqiyatli", response));
    }

    // ==================== LOGIN ====================

    @Operation(
            summary = "Tizimga kirish",
            description = "Telefon/Email va parol orqali tizimga kirish"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Muvaffaqiyatli kirish",
                    content = @Content(schema = @Schema(implementation = AuthResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Noto'g'ri login yoki parol"),
            @ApiResponse(responseCode = "401", description = "Foydalanuvchi faol emas")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<AuthResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request) {
        log.info("Login request");

        AuthResponseDto response = authService.login(request);

        return ResponseEntity.ok(ApiResponseDto.success("Tizimga muvaffaqiyatli kirildi", response));
    }

    // ==================== PASSWORD MANAGEMENT ====================

    @Operation(
            summary = "Parolni unutdim",
            description = "Parolni tiklash uchun tasdiqlash kodi yuborish"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasdiqlash kodi yuborildi"),
            @ApiResponse(responseCode = "404", description = "Foydalanuvchi topilmadi")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponseDto<VerificationSentResponseDto>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDto request) {
        log.info("Forgot password request");

        VerificationSentResponseDto response = authService.forgotPassword(request);

        return ResponseEntity.ok(ApiResponseDto.success("Tasdiqlash kodi yuborildi", response));
    }

    @Operation(
            summary = "Parolni tiklash",
            description = "Tasdiqlash kodi orqali parolni yangilash"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parol muvaffaqiyatli o'zgartirildi"),
            @ApiResponse(responseCode = "400", description = "Tasdiqlash kodi noto'g'ri")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponseDto<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDto request) {
        log.info("Reset password request");

        authService.resetPassword(request);

        return ResponseEntity.ok(ApiResponseDto.success("Parol muvaffaqiyatli o'zgartirildi", null));
    }

    @Operation(
            summary = "Parolni o'zgartirish",
            description = "Joriy foydalanuvchining parolini o'zgartirish"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parol muvaffaqiyatli o'zgartirildi"),
            @ApiResponse(responseCode = "400", description = "Joriy parol noto'g'ri"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab etiladi")
    })
    @SecurityRequirement(name = "bearer-jwt")
    @PostMapping("/change-password")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USER')")
    public ResponseEntity<ApiResponseDto<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequestDto request,
            Authentication authentication) {

        Long userId = ((UserPrincipal) authentication.getPrincipal()).getId();
        log.info("Change password request for user: {}", userId);

        authService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());

        return ResponseEntity.ok(ApiResponseDto.success("Parol muvaffaqiyatli o'zgartirildi", null));
    }

    // ==================== TOKEN MANAGEMENT ====================

    @Operation(
            summary = "Access tokenni yangilash",
            description = "Refresh token orqali yangi access token olish"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token yangilandi"),
            @ApiResponse(responseCode = "401", description = "Refresh token yaroqsiz")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseDto<AuthResponseDto>> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDto request) {
        log.info("Refresh token request");

        AuthResponseDto response = authService.refreshToken(request.getRefreshToken());

        return ResponseEntity.ok(ApiResponseDto.success("Token yangilandi", response));
    }

    @Operation(
            summary = "Tizimdan chiqish",
            description = "Refresh tokenni bekor qilish"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli chiqildi")
    })
    @SecurityRequirement(name = "bearer-jwt")
    @PostMapping("/logout")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USER')")
    public ResponseEntity<ApiResponseDto<Void>> logout(
            @Valid @RequestBody RefreshTokenRequestDto request) {
        log.info("Logout request");

        authService.logout(request.getRefreshToken());

        return ResponseEntity.ok(ApiResponseDto.success("Tizimdan muvaffaqiyatli chiqildi", null));
    }

    // ==================== USER INFO ====================

    @Operation(
            summary = "Joriy foydalanuvchi ma'lumotlari",
            description = "JWT token orqali joriy foydalanuvchi ma'lumotlarini olish"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Foydalanuvchi ma'lumotlari",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))
            ),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab etiladi"),
            @ApiResponse(responseCode = "404", description = "Foydalanuvchi topilmadi")
    })
    @SecurityRequirement(name = "bearer-jwt")
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USER')")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> getCurrentUser(Authentication authentication) {
        Long userId = ((UserPrincipal) authentication.getPrincipal()).getId();
        log.info("Get current user request: {}", userId);

        UserResponseDto user = authService.getCurrentUser(authentication);

        return ResponseEntity.ok(ApiResponseDto.success(user));
    }
}