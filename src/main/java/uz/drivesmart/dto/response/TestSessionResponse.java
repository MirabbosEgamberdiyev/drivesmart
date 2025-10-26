package uz.drivesmart.dto.response;

public record TestSessionResponse(
        Long id,
        String topic,
        int totalQuestions,
        int score
) {}