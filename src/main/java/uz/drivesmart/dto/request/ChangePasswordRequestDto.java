package uz.drivesmart.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Parol o'zgartirish uchun DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequestDto {

    @NotBlank(message = "Joriy parol majburiy")
    private String currentPassword;

    @NotBlank(message = "Yangi parol majburiy")
    @Size(min = 6, max = 50, message = "Parol 6-50 belgi oralig'ida bo'lishi kerak")
    private String newPassword;

    @NotBlank(message = "Parol tasdiqlash majburiy")
    private String confirmPassword;

    @AssertTrue(message = "Yangi parol va tasdiq parol mos kelmadi")
    public boolean isPasswordMatching() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}