package uz.drivesmart.dto.response;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Barcha javoblarni bir vaqtda yuborish uchun
 */
public record SubmitAllAnswersRequest(
        @NotNull(message = "Test session ID bo'sh bo'lmasligi kerak")
        Long sessionId,

        @NotEmpty(message = "Javoblar ro'yxati bo'sh bo'lmasligi kerak")
        @Valid
        List<UserAnswerDto> answers
) {}