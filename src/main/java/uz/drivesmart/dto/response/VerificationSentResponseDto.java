package uz.drivesmart.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
        * Tasdiqlash kodi yuborilgani haqida javob
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationSentResponseDto {

    @Schema(description = "Kod yuborilgan manzil", example = "998901234567")
    private String recipient;

    @Schema(description = "Kod amal qilish muddati (daqiqalarda)", example = "5")
    private Integer expiresInMinutes;

    @Schema(description = "Keyingi kod yuborish uchun kutish vaqti (sekundlarda)", example = "60")
    private Integer retryAfterSeconds;

    @Schema(description = "Xabar", example = "Tasdiqlash kodi yuborildi")
    private String message;
}