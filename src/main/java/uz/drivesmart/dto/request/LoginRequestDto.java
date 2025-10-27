package uz.drivesmart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Kirish uchun request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDto {

    @NotBlank(message = "Telefon raqami majburiy")
    @Pattern(regexp = "^998[0-9]{9}$", message = "Telefon raqami noto'g'ri formatda")
    @Schema(example = "998889996499", description = "Telefon raqami")
    private String phoneNumber;

    @NotBlank(message = "Parol majburiy")
    @Size(min = 6, message = "Parol kamida 6 ta belgidan iborat bo'lishi kerak")
    @Schema(example = "Admin.123$", description = "Foydalanuvchi paroli")
    private String password;
}