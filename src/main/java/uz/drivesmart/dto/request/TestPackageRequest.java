package uz.drivesmart.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record TestPackageRequest(
        @NotBlank(message = "Paket nomi majburiy")
        @Size(max = 200, message = "Paket nomi 200 belgidan oshmasligi kerak")
        String name,

        @Size(max = 2000, message = "Tavsif 2000 belgidan oshmasligi kerak")
        String description,

        @NotNull(message = "Narx majburiy")
        @DecimalMin(value = "0.01", message = "Narx 0 dan katta bo'lishi kerak")
        BigDecimal price,

        @NotNull(message = "Savollar soni majburiy")
        @Min(value = 1, message = "Kamida 1 ta savol bo'lishi kerak")
        @Max(value = 100, message = "Maksimal 100 ta savol bo'lishi mumkin")
        Integer questionCount,

        @NotNull(message = "Amal qilish muddati majburiy")
        @Min(value = 1, message = "Kamida 1 kun amal qilishi kerak")
        @Max(value = 365, message = "Maksimal 1 yil amal qilishi mumkin")
        Integer durationDays,

        @NotNull(message = "Urinishlar soni majburiy")
        @Min(value = 1, message = "Kamida 1 ta urinish bo'lishi kerak")
        @Max(value = 10, message = "Maksimal 10 ta urinish bo'lishi mumkin")
        Integer maxAttempts,

        @NotBlank(message = "Mavzu majburiy")
        @Size(max = 100, message = "Mavzu 100 belgidan oshmasligi kerak")
        String topic
) {}
