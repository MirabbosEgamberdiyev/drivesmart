package uz.drivesmart.dto.response;

public record AnswerResultResponse(
        boolean correct,
        String correctAnswer,
        int score
) {}