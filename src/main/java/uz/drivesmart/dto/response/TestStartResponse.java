package uz.drivesmart.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Test boshlanganda qaytariladigan to'liq ma'lumot
 * ptest.uz formatiga mos
 */
public record TestStartResponse(
        Long sessionId,
        String topic,
        Integer totalQuestions,
        Integer durationMinutes,
        LocalDateTime startedAt,
        LocalDateTime expiresAt,
        List<QuestionForTestDto> questions
) {}