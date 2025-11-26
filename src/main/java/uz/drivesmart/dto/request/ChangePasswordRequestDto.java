package uz.drivesmart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Parolni o'zgartirish (autentifikatsiya qilingan foydalanuvchi)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordRequestDto {

    @NotBlank(message = "Joriy parol majburiy")
    @Schema(example = "OldPass123", description = "Joriy parol")
    private String currentPassword;

    @NotBlank(message = "Yangi parol majburiy")
    @Size(min = 6, max = 50, message = "Parol 6-50 belgi oralig'ida bo'lishi kerak")
    @Schema(example = "NewPass456", description = "Yangi parol")
    private String newPassword;
}