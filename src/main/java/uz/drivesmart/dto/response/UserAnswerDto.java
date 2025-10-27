package uz.drivesmart.dto.response;

import jakarta.validation.constraints.NotNull;

public record UserAnswerDto(
        @NotNull Long questionId,
        @NotNull String selectedAnswer
) {}