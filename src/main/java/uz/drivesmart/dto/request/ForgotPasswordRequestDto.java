package uz.drivesmart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.drivesmart.enums.VerificationType;

/**
 * Parolni unutdim - boshlang'ich so'rov
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForgotPasswordRequestDto {

    @Schema(example = "998901234567", description = "Telefon raqami")
    private String phoneNumber;

    @Email
    @Schema(example = "mirabbos@example.com", description = "Email")
    private String email;

    @NotNull(message = "Tasdiqlash turi majburiy")
    @Schema(example = "SMS", description = "SMS yoki EMAIL")
    private VerificationType verificationType;
}