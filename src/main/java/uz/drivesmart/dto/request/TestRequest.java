package uz.drivesmart.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TestRequest(
        @NotBlank(message = "Mavzu kiritilishi shart")
        @Size(max = 100, message = "Mavzu nomi 100 belgidan oshmasligi kerak")
        String topic,

        @Min(value = 1, message = "Savollar soni kamida 1 bo'lishi kerak")
        @Max(value = 50, message = "Savollar soni 50 tadan oshmasligi kerak")
        int questionCount
) {}