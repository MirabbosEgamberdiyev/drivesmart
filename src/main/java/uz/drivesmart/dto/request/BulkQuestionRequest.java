package uz.drivesmart.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.Valid;

import java.util.List;

public record BulkQuestionRequest(
        @NotEmpty(message = "Savollar ro'yxati bo'sh bo'lmasligi kerak")
        @Valid
        List<QuestionRequest> questions
) {}