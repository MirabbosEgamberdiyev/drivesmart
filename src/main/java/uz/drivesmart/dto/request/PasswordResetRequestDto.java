package uz.drivesmart.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Parol reset DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequestDto {

    @NotBlank(message = "Yangi parol majburiy")
    @Size(min = 6, max = 50, message = "Parol 6-50 belgi oralig'ida bo'lishi kerak")
    private String newPassword;
}