package uz.drivesmart.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import uz.drivesmart.dto.response.UserAnswerDto;

import java.util.List;

public record SubmitAllAnswersRequest(
        @NotNull Long sessionId,

        @NotEmpty(message = "Javoblar bo'sh bo'lmasligi kerak")
        @Valid
        List<UserAnswerDto> answers
) {}