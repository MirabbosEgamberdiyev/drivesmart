package uz.drivesmart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.drivesmart.enums.VerificationType;

// ========== REGISTRATION ==========

/**
 * Ro'yxatdan o'tish boshlang'ich so'rov
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterInitRequestDto {

    @NotBlank(message = "Ism majburiy")
    @Size(min = 2, max = 50, message = "Ism 2-50 belgi oralig'ida bo'lishi kerak")
    @Schema(example = "Mirabbos", description = "Foydalanuvchi ismi")
    private String firstName;

    @Size(max = 50, message = "Familiya 50 ta belgidan oshmasligi kerak")
    @Schema(example = "Egamberdiyev", description = "Foydalanuvchi familiyasi")
    private String lastName;

    @Pattern(regexp = "^998[0-9]{9}$", message = "Telefon raqami noto'g'ri formatda")
    @Schema(example = "998901234567", description = "Telefon raqami (SMS uchun)")
    private String phoneNumber;

    @Email(message = "Email noto'g'ri formatda")
    @Schema(example = "mirabbos@example.com", description = "Email manzili")
    private String email;

    @NotNull(message = "Tasdiqlash turi majburiy")
    @Schema(example = "SMS", description = "Tasdiqlash turi: SMS yoki EMAIL")
    private VerificationType verificationType;

    @NotBlank(message = "Parol majburiy")
    @Size(min = 6, max = 50, message = "Parol 6-50 belgi oralig'ida bo'lishi kerak")
    @Schema(example = "SecurePass123", description = "Foydalanuvchi paroli")
    private String password;
}
