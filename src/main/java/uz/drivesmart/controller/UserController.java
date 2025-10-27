package uz.drivesmart.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.drivesmart.dto.request.PasswordResetRequestDto;
import uz.drivesmart.dto.request.UserRequestDto;
import uz.drivesmart.dto.request.UserStatusRequestDto;
import uz.drivesmart.dto.request.UserUpdateRequestDto;
import uz.drivesmart.dto.response.ApiResponseDto;
import uz.drivesmart.dto.response.UserResponseDto;
import uz.drivesmart.enums.Role;
import uz.drivesmart.service.impl.UserService;


import java.util.List;

/**
 * Foydalanuvchi boshqaruvi controller
 */
@RestController
@RequestMapping("/api/v1/users")
@Slf4j
@Validated
@Tag(name = "User Management", description = "Foydalanuvchilar boshqaruvi API")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Barcha faol foydalanuvchilarni olish",
            description = "Tizimda ro'yxatdan o'tgan barcha faol foydalanuvchilar ro'yxatini qaytaradi. " +
                    "O'chirilgan yoki bloklangan foydalanuvchilar ko'rsatilmaydi.",
            tags = {"User Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Foydalanuvchilar ro'yxati muvaffaqiyatli olindi",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Foydalanuvchilarni ko'rish huquqi yo'q",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Server xatosi",
                    content = @Content
            )
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<List<UserResponseDto>>> getAllUsers() {
        log.info("Get all users request");

        List<UserResponseDto> users = userService.getAllActiveUsers();

        return ResponseEntity.ok(ApiResponseDto.success(users));
    }

    @Operation(
            summary = "Foydalanuvchini ID bo'yicha olish",
            description = "Berilgan ID bo'yicha foydalanuvchi ma'lumotlarini qaytaradi. " +
                    "Foydalanuvchi profil ma'lumotlari, roli va holati haqida to'liq ma'lumot beradi.",
            tags = {"User Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Foydalanuvchi topildi",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Foydalanuvchi topilmadi",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Foydalanuvchi ma'lumotlarini ko'rish huquqi yo'q",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> getUserById(
            @Parameter(description = "Foydalanuvchi ID raqami", required = true, example = "123")
            @PathVariable Long id) {
        log.info("Get user by ID request: {}", id);

        UserResponseDto user = userService.getUserById(id);

        return ResponseEntity.ok(ApiResponseDto.success(user));
    }

    @Operation(
            summary = "Yangi foydalanuvchi yaratish",
            description = "Tizimga yangi foydalanuvchi qo'shadi. Foydalanuvchi ma'lumotlari, roli va boshlang'ich parolni belgilaydi. " +
                    "Telefon raqami unikal bo'lishi kerak.",
            tags = {"User Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Foydalanuvchi muvaffaqiyatli yaratildi",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Noto'g'ri ma'lumotlar yoki telefon raqami allaqachon mavjud",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Foydalanuvchi yaratish huquqi yo'q",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Validatsiya xatolari",
                    content = @Content
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> createUser(
            @Parameter(description = "Yangi foydalanuvchi ma'lumotlari", required = true)
            @Valid @RequestBody UserRequestDto request) {
        log.info("Create user request: {}", request.getPhoneNumber());

        UserResponseDto user = userService.createUser(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Foydalanuvchi muvaffaqiyatli yaratildi", user));
    }

    @Operation(
            summary = "Foydalanuvchi ma'lumotlarini yangilash",
            description = "Mavjud foydalanuvchining profil ma'lumotlarini yangilaydi. " +
                    "Ism, telefon raqami, rol va boshqa ma'lumotlarni o'zgartirish mumkin.",
            tags = {"User Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Foydalanuvchi ma'lumotlari yangilandi",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Noto'g'ri ma'lumotlar",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Foydalanuvchi topilmadi",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Yangilash huquqi yo'q",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Validatsiya xatolari",
                    content = @Content
            )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> updateUser(
            @Parameter(description = "Yangilanayotgan foydalanuvchi ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Yangilash ma'lumotlari", required = true)
            @Valid @RequestBody UserUpdateRequestDto request) {
        log.info("Update user request for ID: {}", id);

        UserResponseDto user = userService.updateUser(id, request);

        return ResponseEntity.ok(ApiResponseDto.success("Foydalanuvchi muvaffaqiyatli yangilandi", user));
    }

    @Operation(
            summary = "Foydalanuvchini o'chirish",
            description = "Foydalanuvchini tizimdan butunlay o'chiradi. Bu amal qaytarib bo'lmaydi. " +
                    "Faqat super admin roli bo'lgan foydalanuvchilar bajarishlari mumkin.",
            tags = {"User Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Foydalanuvchi muvaffaqiyatli o'chirildi",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Foydalanuvchi topilmadi",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "O'chirish huquqi yo'q - faqat super admin",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Foydalanuvchini o'chirish mumkin emas (faol tranzaksiyalar mavjud)",
                    content = @Content
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDto<Void>> deleteUser(
            @Parameter(description = "O'chiriladigan foydalanuvchi ID", required = true)
            @PathVariable Long id) {
        log.info("Delete user request for ID: {}", id);

        userService.deleteUser(id);

        return ResponseEntity.ok(ApiResponseDto.success("Foydalanuvchi muvaffaqiyatli o'chirildi", null));
    }

    @Operation(
            summary = "Rol bo'yicha foydalanuvchilarni olish",
            description = "Belgilangan rolga ega barcha foydalanuvchilar ro'yxatini qaytaradi. " +
                    "Masalan, barcha sotuvchilar yoki adminlarni ko'rish uchun ishlatiladi.",
            tags = {"User Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Rol bo'yicha foydalanuvchilar ro'yxati",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Noto'g'ri rol nomi",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Rol bo'yicha qidiruv huquqi yo'q",
                    content = @Content
            )
    })
    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<List<UserResponseDto>>> getUsersByRole(
            @Parameter(description = "Foydalanuvchi roli", required = true,
                    example = "USER",
                    schema = @Schema(allowableValues = {"SUPER_ADMIN", "ADMIN", "USER"}))
            @PathVariable Role role) {
        log.info("Get users by role request: {}", role);

        List<UserResponseDto> users = userService.getUsersByRole(role);

        return ResponseEntity.ok(ApiResponseDto.success(users));
    }

    @Operation(
            summary = "Foydalanuvchi holatini o'zgartirish",
            description = "Foydalanuvchini faollashtirish yoki bloklaash. " +
                    "Bloklangan foydalanuvchilar tizimga kira olmaydi.",
            tags = {"User Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Foydalanuvchi holati o'zgartirildi",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Foydalanuvchi topilmadi",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Holat o'zgartirish huquqi yo'q",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Noto'g'ri holat qiymati",
                    content = @Content
            )
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<Void>> toggleUserStatus(
            @Parameter(description = "Foydalanuvchi ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Yangi holat ma'lumotlari", required = true)
            @Valid @RequestBody UserStatusRequestDto request) {
        log.info("Toggle user status request for ID: {} to {}", id, request.getIsActive());

        userService.toggleUserStatus(id, request.getIsActive());

        return ResponseEntity.ok(ApiResponseDto.success("Foydalanuvchi holati o'zgartirildi", null));
    }

    @Operation(
            summary = "Foydalanuvchi parolini qayta tiklash",
            description = "Foydalanuvchi parolini adminstrativ tarzda o'zgartiradi. " +
                    "Yangi parol darhol kuchga kiradi va foydalanuvchi keyingi kirishda uni o'zgartirishlari mumkin.",
            tags = {"User Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Parol muvaffaqiyatli o'zgartirildi",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Foydalanuvchi topilmadi",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Parol o'zgartirish huquqi yo'q - faqat super admin",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Parol talablarga mos kelmaydi",
                    content = @Content
            )
    })
    @PatchMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDto<Void>> resetUserPassword(
            @Parameter(description = "Foydalanuvchi ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Yangi parol ma'lumotlari", required = true)
            @Valid @RequestBody PasswordResetRequestDto request) {
        log.info("Reset password request for user ID: {}", id);

        userService.resetUserPassword(id, request.getNewPassword());

        return ResponseEntity.ok(ApiResponseDto.success("Parol muvaffaqiyatli o'zgartirildi", null));
    }
}