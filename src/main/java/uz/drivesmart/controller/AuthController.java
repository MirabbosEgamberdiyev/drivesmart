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
import uz.drivesmart.dto.request.ChangePasswordRequestDto;
import uz.drivesmart.dto.request.LoginRequestDto;
import uz.drivesmart.dto.request.RegisterRequestDto;
import uz.drivesmart.dto.response.ApiResponseDto;
import uz.drivesmart.dto.response.AuthResponseDto;
import uz.drivesmart.dto.response.UserResponseDto;
import uz.drivesmart.exception.CustomException;
import uz.drivesmart.security.UserPrincipal;
import uz.drivesmart.service.impl.AuthService;


/**
 * Autentifikatsiya va avtorizatsiya boshqaruvi
 */
@Tag(name = "Authentication And Authorization", description = "Foydalanuvchi kirishi, chiqishi va parol boshqaruvi")
@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
@Validated
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Ro'yxatdan o'tish",
            description = "Yangi foydalanuvchi ro'yxatdan o'tishi. Role avtomatik USER bo'ladi"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ro'yxatdan o'tish muvaffaqiyatli"),
            @ApiResponse(responseCode = "400", description = "Telefon raqami allaqachon mavjud"),
            @ApiResponse(responseCode = "422", description = "Validatsiya xatolari")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponseDto<AuthResponseDto>> register(
            @Valid @RequestBody RegisterRequestDto request) {
        log.info("Registration request: {}", request.getPhoneNumber());

        AuthResponseDto response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Ro'yxatdan o'tish muvaffaqiyatli", response));
    }

    @Operation(
            summary = "Tizimga kirish",
            description = "Telefon raqam va parol orqali tizimga kirish va JWT token olish"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Muvaffaqiyatli kirish",
                    content = @Content(schema = @Schema(implementation = AuthResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Noto'g'ri telefon raqam yoki parol",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Foydalanuvchi faol emas",
                    content = @Content
            )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<AuthResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request) {
        log.info("Tizimga kirish so'rovi: {}", request.getPhoneNumber());

        AuthResponseDto response = authService.login(request);

        return ResponseEntity.ok(ApiResponseDto.success("Tizimga muvaffaqiyatli kirildi", response));
    }

    @Operation(
            summary = "Parolni o'zgartirish",
            description = "Joriy foydalanuvchining parolini o'zgartirish"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Parol muvaffaqiyatli o'zgartirildi"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Joriy parol noto'g'ri"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autentifikatsiya talab etiladi"
            )
    })
    @SecurityRequirement(name = "bearer-jwt")
    @PostMapping("/change-password")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USER')")
    public ResponseEntity<ApiResponseDto<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequestDto request,
            Authentication authentication) {

        Long userId = ((UserPrincipal) authentication.getPrincipal()).getId();
        log.info("Parol o'zgartirish so'rovi: {}", userId);

        authService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());

        return ResponseEntity.ok(ApiResponseDto.success("Parol muvaffaqiyatli o'zgartirildi", null));
    }

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
            @ApiResponse(
                    responseCode = "401",
                    description = "Autentifikatsiya talab etiladi"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Foydalanuvchi topilmadi"
            )
    })
    @SecurityRequirement(name = "bearer-jwt")
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USER')")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> getCurrentUser(Authentication authentication) throws CustomException {
        Long userId = ((UserPrincipal) authentication.getPrincipal()).getId();
        log.info("Joriy foydalanuvchi ma'lumotlari so'rovi: {}", userId);

        UserResponseDto user = authService.getCurrentUser(authentication);

        return ResponseEntity.ok(ApiResponseDto.success(user));
    }
}