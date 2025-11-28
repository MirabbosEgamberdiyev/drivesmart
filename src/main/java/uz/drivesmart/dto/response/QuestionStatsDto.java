package uz.drivesmart.dto.response;

public record QuestionStatsDto(
        Long questionI,
        String questionText,
        Long totalAttempts,
        Long correctAttempts,
        Double successRate
) {}
