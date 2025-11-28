package uz.drivesmart.dto.response;
import java.time.LocalDateTime;

public record TestResultResponse(
        Long sessionId,
        String topic,
        Integer totalQuestions,
        Integer correctCount,
        Integer score,
        Double percentage,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {}