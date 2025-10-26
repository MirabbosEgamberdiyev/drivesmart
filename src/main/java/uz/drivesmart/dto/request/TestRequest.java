package uz.drivesmart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record TestRequest(
        @NotBlank(message = "Mavzu kiritilishi shart")
        String topic,

        @Min(value = 1, message = "Savollar soni kamida 1 bo‘lishi kerak")
        int questionCount
) {}