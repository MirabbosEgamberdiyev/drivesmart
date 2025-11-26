package uz.drivesmart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Login qilish (telefon yoki email orqali)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDto {

    @Schema(example = "998901234567", description = "Telefon yoki email")
    private String phoneNumber;

    @Email
    @Schema(example = "mirabbos@example.com")
    private String email;

    @NotBlank(message = "Parol majburiy")
    @Size(min = 6, message = "Parol kamida 6 ta belgidan iborat bo'lishi kerak")
    @Schema(example = "SecurePass123", description = "Foydalanuvchi paroli")
    private String password;
}