package uz.drivesmart.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmitAnswerRequest(
        @NotNull Long questionId,
        @NotBlank String answer
) {}