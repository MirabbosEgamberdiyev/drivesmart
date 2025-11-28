package uz.drivesmart.dto.response;

public record DashboardStatsDto(
        Long totalUsers,
        Long totalQuestions,
        Long totalTests,
        Long completedTests,
        Long activeTests,
        Double averageScore,
        Long testsToday,
        Long testsThisWeek,
        Long testsThisMonth
) {}