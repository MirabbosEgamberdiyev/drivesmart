package uz.drivesmart.dto.response;


import java.time.LocalDateTime;

public record TestSessionResponse(
        Long id,
        String topic,
        Integer totalQuestions,
        Integer score,
        Integer correctCount,
        Integer wrongCount,
        String status,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {}
