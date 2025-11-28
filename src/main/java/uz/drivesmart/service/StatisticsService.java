package uz.drivesmart.service;

import uz.drivesmart.dto.response.QuestionStatsDto;
import uz.drivesmart.dto.response.UserProgressDto;

import java.util.List;

public interface StatisticsService {
    List<UserProgressDto> getUserProgress(Long userId);
    List<UserProgressDto> getTopPerformers(String topic, int limit);
    List<QuestionStatsDto> getDifficultQuestions(String topic);
}