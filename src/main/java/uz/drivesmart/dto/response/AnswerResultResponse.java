package uz.drivesmart.dto.response;

public record AnswerResultResponse(
        boolean isCorrect,
        String correctAnswer,
        Integer currentScore
) {}