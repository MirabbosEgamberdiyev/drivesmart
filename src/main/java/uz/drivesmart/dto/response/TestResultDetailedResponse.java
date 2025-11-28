package uz.drivesmart.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Test natijasi batafsil ma'lumot bilan
 * ptest.uz formatiga mos
 */
public record TestResultDetailedResponse(
        Long sessionId,
        String topic,
        Integer totalQuestions,
        Integer correctCount,
        Integer wrongCount,
        Integer score,
        Double percentage,
        boolean passed,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        Long durationSeconds,
        List<AnswerDetailDto> answers
) {}
