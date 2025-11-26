package uz.drivesmart.dto.request;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.drivesmart.enums.VerificationType;

/**
 * Parolni tiklash (kod tasdiqlashdan keyin)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequestDto {

    @NotBlank(message = "Recipient majburiy")
    @Schema(example = "998901234567", description = "Telefon yoki email")
    private String recipient;

    @NotBlank(message = "Kod majburiy")
    @Size(min = 6, max = 6, message = "Kod 6 ta raqamdan iborat")
    @Schema(example = "123456", description = "6 xonali tasdiqlash kodi")
    private String code;

    @NotBlank(message = "Yangi parol majburiy")
    @Size(min = 6, max = 50, message = "Parol 6-50 belgi oralig'ida bo'lishi kerak")
    @Schema(example = "NewSecurePass456", description = "Yangi parol")
    private String newPassword;

    @NotNull(message = "Tasdiqlash turi majburiy")
    @Schema(example = "SMS")
    private VerificationType verificationType;
}