package uz.drivesmart.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.drivesmart.dto.response.QuestionStatsDto;
import uz.drivesmart.dto.response.UserProgressDto;
import uz.drivesmart.repository.QuestionStatisticsRepository;
import uz.drivesmart.repository.UserProgressRepository;
import uz.drivesmart.service.StatisticsService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final UserProgressRepository progressRepository;
    private final QuestionStatisticsRepository statsRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserProgressDto> getUserProgress(Long userId) {
        return progressRepository.findByUserId(userId).stream()
                .map(p -> new UserProgressDto(
                        p.getUser().getId(),
                        p.getUser().getFirstName() + " " + p.getUser().getLastName(),
                        p.getTopic(),
                        p.getTotalTests(),
                        p.getPassedTests(),
                        p.getAverageScore(),
                        p.getBestScore(),
                        p.getCurrentStreak(),
                        p.getLongestStreak()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProgressDto> getTopPerformers(String topic, int limit) {
        return progressRepository.findTopPerformersByTopic(topic).stream()
                .limit(limit)
                .map(p -> new UserProgressDto(
                        p.getUser().getId(),
                        p.getUser().getFirstName() + " " + p.getUser().getLastName(),
                        p.getTopic(),
                        p.getTotalTests(),
                        p.getPassedTests(),
                        p.getAverageScore(),
                        p.getBestScore(),
                        p.getCurrentStreak(),
                        p.getLongestStreak()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionStatsDto> getDifficultQuestions(String topic) {
        return statsRepository.findDifficultQuestions(topic, 60.0).stream()
                .map(qs -> new QuestionStatsDto(
                        qs.getQuestion().getId(),
                        qs.getQuestion().getText(),
                        qs.getTotalAttempts(),
                        qs.getCorrectAttempts(),
                        qs.getSuccessRate()
                ))
                .collect(Collectors.toList());
    }
}