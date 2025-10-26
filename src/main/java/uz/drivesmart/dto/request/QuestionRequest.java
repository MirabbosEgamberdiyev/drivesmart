package uz.drivesmart.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record QuestionRequest(
        @NotBlank(message = "Savol matni bo'sh bo'lmasligi kerak")
        String text,

        @NotEmpty(message = "Javob variantlari bo'sh bo'lmasligi kerak")
        List<String> options,

        @NotBlank(message = "To'g'ri javob bo'sh bo'lmasligi kerak")
        String correctAnswer,

        @NotBlank(message = "Mavzu bo'sh bo'lmasligi kerak")
        String topic
) {}