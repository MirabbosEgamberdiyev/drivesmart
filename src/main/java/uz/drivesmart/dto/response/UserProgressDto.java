package uz.drivesmart.dto.response;

public record UserProgressDto(
        Long userId,
        String userName,
        String topic,
        Integer totalTests,
        Integer passedTests,
        Double averageScore,
        Double bestScore,
        Integer currentStreak,
        Integer longestStreak
) {}