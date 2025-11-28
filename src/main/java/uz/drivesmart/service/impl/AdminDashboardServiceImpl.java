package uz.drivesmart.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.drivesmart.dto.response.DashboardStatsDto;
import uz.drivesmart.dto.response.TestSessionResponse;
import uz.drivesmart.dto.response.TopicStatsDto;
import uz.drivesmart.enums.TestStatus;
import uz.drivesmart.repository.*;
import uz.drivesmart.service.AdminDashboardService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final TestSessionRepository sessionRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        Long totalUsers = userRepository.count();
        Long totalQuestions = questionRepository.count();
        Long totalTests = sessionRepository.count();
        Long completedTests = sessionRepository.countByStatus(TestStatus.COMPLETED);
        Long activeTests = sessionRepository.countByStatus(TestStatus.IN_PROGRESS);

        Double averageScore = sessionRepository.calculateAverageScore();

        LocalDateTime today = LocalDate.now().atStartOfDay();
        LocalDateTime weekStart = today.minusDays(7);
        LocalDateTime monthStart = today.minusDays(30);

        Long testsToday = sessionRepository.countByStartedAtAfter(today);
        Long testsThisWeek = sessionRepository.countByStartedAtAfter(weekStart);
        Long testsThisMonth = sessionRepository.countByStartedAtAfter(monthStart);

        return new DashboardStatsDto(
                totalUsers,
                totalQuestions,
                totalTests,
                completedTests,
                activeTests,
                averageScore != null ? averageScore : 0.0,
                testsToday,
                testsThisWeek,
                testsThisMonth
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestSessionResponse> getRecentTests(int limit) {
        return sessionRepository.findRecentTests(limit).stream()
                .map(session -> new TestSessionResponse(
                        session.getId(),
                        session.getTopic(),
                        session.getTotalQuestions(),
                        session.getScore(),
                        session.getCorrectCount(),
                        session.getWrongCount(),
                        session.getStatus().name(),
                        session.getStartedAt(),
                        session.getFinishedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopicStatsDto> getTopicStats() {
        return sessionRepository.calculateTopicStats();
    }
}