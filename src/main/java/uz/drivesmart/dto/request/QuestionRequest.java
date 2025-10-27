package uz.drivesmart.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

// ✅ QuestionRequest
public record QuestionRequest(
        @NotBlank(message = "Savol matni bo'sh bo'lmasligi kerak")
        @Size(max = 1000, message = "Savol matni 1000 belgidan oshmasligi kerak")
        String text,

        @NotEmpty(message = "Javob variantlari bo'sh bo'lmasligi kerak")
        @Size(min = 2, max = 6, message = "2-6 ta javob varianti bo'lishi kerak")
        List<@NotBlank(message = "Variant bo'sh bo'lmasligi kerak") String> options,

        @NotBlank(message = "To'g'ri javob bo'sh bo'lmasligi kerak")
        String correctAnswer,

        @NotBlank(message = "Mavzu bo'sh bo'lmasligi kerak")
        @Size(max = 100, message = "Mavzu 100 belgidan oshmasligi kerak")
        String topic,

        @Size(max = 10485760, message = "Rasm hajmi 10MB dan oshmasligi kerak") // Base64 uchun
        String imageBase64
) {
        // ✅ Custom validation
        public QuestionRequest {
                if (options != null && !options.contains(correctAnswer)) {
                        throw new IllegalArgumentException("To'g'ri javob variantlar orasida bo'lishi kerak");
                }
        }
}