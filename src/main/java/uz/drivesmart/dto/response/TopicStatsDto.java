package uz.drivesmart.dto.response;

public record TopicStatsDto(
        String topic,
        Long totalTests,
        Double averageScore,
        Long passedTests
) {}