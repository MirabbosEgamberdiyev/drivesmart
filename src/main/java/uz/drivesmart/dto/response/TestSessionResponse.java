package uz.drivesmart.dto.response;


import java.time.LocalDateTime;

public record TestSessionResponse(
        Long id,
        String topic,
        int totalQuestions,
        int score,
        int correctCount,
        int wrongCount,
        String status,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {}
