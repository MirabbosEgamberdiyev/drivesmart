package uz.drivesmart.dto.response;
import java.time.LocalDateTime;

public record TestResultResponse(
        Long sessionId,
        String topic,
        int total,
        int correct,
        int score,
        double percentage,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {}