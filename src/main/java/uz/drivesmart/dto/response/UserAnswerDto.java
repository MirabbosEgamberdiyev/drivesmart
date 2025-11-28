package uz.drivesmart.dto.response;

import jakarta.validation.constraints.NotNull;


import jakarta.validation.constraints.NotBlank;

public record UserAnswerDto(
        @NotNull Long questionId,
        @NotBlank String selectedAnswer
) {}