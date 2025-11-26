package uz.drivesmart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import uz.drivesmart.enums.VerificationType;

/**
 * Tasdiqlash kodi yuborish
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyCodeRequestDto {

    @NotBlank(message = "Recipient majburiy")
    @Schema(example = "998901234567", description = "Telefon yoki email")
    private String recipient;

    @NotBlank(message = "Kod majburiy")
    @Size(min = 6, max = 6, message = "Kod 6 ta raqamdan iborat bo'lishi kerak")
    @Pattern(regexp = "^[0-9]{6}$", message = "Kod faqat raqamlardan iborat bo'lishi kerak")
    @Schema(example = "123456", description = "6 xonali tasdiqlash kodi")
    private String code;

    @NotNull(message = "Tasdiqlash turi majburiy")
    @Schema(example = "SMS", description = "Tasdiqlash turi")
    private VerificationType verificationType;
}
